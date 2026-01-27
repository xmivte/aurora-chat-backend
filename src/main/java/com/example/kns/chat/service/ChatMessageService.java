package com.example.kns.chat.service;

import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import com.example.kns.file.config.FileStorageProperties;
import com.example.kns.file.dto.FileMetadataDTO;
import com.example.kns.file.model.FileAttachment;
import com.example.kns.file.repository.FileAttachmentRepository;
import com.example.kns.file.service.FileStorageService;
import com.example.kns.group.repository.GroupRepository;
import com.example.kns.notifications.NotificationService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
@Service
@RequiredArgsConstructor
@Validated
public class ChatMessageService {

	private final ChatMessagesRepository repository;
	private final NotificationService notificationService;
	private final FileAttachmentRepository fileAttachmentRepository;
	private final FileStorageService fileStorageService;
	private final FileStorageProperties config;
	private final GroupRepository groupRepository;

	@Transactional
	public ChatMessage saveIncoming(@Valid ChatMessageDTO dto) {
		if (!groupRepository.isUserInGroup(dto.getSenderId(), dto.getGroupId())) {
			log.warn("User {} attempted to send message to group {} without being a member", dto.getSenderId(),
					dto.getGroupId());
			throw new AccessDeniedException("You are not a member of this group");
		}

		ChatMessage msg = ChatMessage.builder().senderId(dto.getSenderId()).groupId(dto.getGroupId())
				.content(dto.getContent()).sent(false).build();

		repository.insert(msg);

		List<String> uploadedFileNames = new ArrayList<>();

		try {
			if (dto.getFileMetadata() != null && !dto.getFileMetadata().isEmpty()) {
				OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(config.getExpirationDays());

				for (FileMetadataDTO fileMetadata : dto.getFileMetadata()) {
					if (fileMetadata.getFileName() == null || fileMetadata.getFileName().isBlank()) {
						throw new IllegalArgumentException("File metadata is missing fileName");
					}
					if (fileMetadata.getOriginalFileName() == null || fileMetadata.getOriginalFileName().isBlank()) {
						throw new IllegalArgumentException("File metadata is missing originalFileName");
					}
					if (fileMetadata.getFileUrl() == null || fileMetadata.getFileUrl().isBlank()) {
						throw new IllegalArgumentException("File metadata is missing fileUrl");
					}

					uploadedFileNames.add(fileMetadata.getFileName());

					FileAttachment fileAttachment = FileAttachment.builder().messageId(msg.getId())
							.fileName(fileMetadata.getFileName()).originalFileName(fileMetadata.getOriginalFileName())
							.fileUrl(fileMetadata.getFileUrl()).fileType(fileMetadata.getFileType())
							.fileSize(fileMetadata.getFileSize()).expiresAt(expiresAt).build();

					fileAttachmentRepository.insert(fileAttachment);
				}
			}

			notificationService.onNewMessageSaved(msg.getGroupId(), msg.getSenderId(), msg.getId(), msg.getContent());
			return msg;

		} catch (Exception e) {
			log.error(
					"Failed to save message or file attachments. Rolling back and deleting {} uploaded files from Cloudinary",
					uploadedFileNames.size());

			for (String fileName : uploadedFileNames) {
				try {
					fileStorageService.deleteFile(fileName);
					log.debug("Deleted file from Cloudinary during rollback: {}", fileName);
				} catch (Exception deleteError) {
					log.error("Failed to delete file from Cloudinary during rollback: {}, error: {}", fileName,
							deleteError.getMessage());
				}
			}
			throw new RuntimeException("Failed to save message with attachments", e);
		}
	}

	@Transactional(readOnly = true)
	public List<ChatMessage> getAll(@NotBlank String groupId) {
		List<ChatMessage> messages = repository.findAllMessagesByGroupId(groupId);

		if (messages.isEmpty()) {
			return messages;
		}

		List<Long> messageIds = messages.stream().map(ChatMessage::getId).collect(Collectors.toList());

		Map<Long, List<FileAttachment>> attachmentsByMessageId = new HashMap<>();
		List<FileAttachment> allAttachments = fileAttachmentRepository.findByMessageIds(messageIds);
		for (FileAttachment attachment : allAttachments) {
			attachmentsByMessageId.computeIfAbsent(attachment.getMessageId(), k -> new ArrayList<>()).add(attachment);
		}

		for (ChatMessage msg : messages) {
			msg.setFileAttachments(attachmentsByMessageId.getOrDefault(msg.getId(), List.of()));
		}

		return messages;
	}

	@Transactional
	public void deleteMessage(Long messageId, String userId) {
		ChatMessage message = repository.findById(messageId);
		if (message == null) {
			throw new IllegalArgumentException("Message not found");
		}

		if (!groupRepository.isUserInGroup(userId, message.getGroupId())) {
			throw new IllegalArgumentException("You do not have permission to delete this message");
		}

		List<FileAttachment> attachments = fileAttachmentRepository.findByMessageId(messageId);

		for (FileAttachment attachment : attachments) {
			try {
				fileStorageService.deleteFile(attachment.getFileName());
				log.debug("Deleted file from Cloudinary: {}", attachment.getFileName());
			} catch (Exception e) {
				log.error("Failed to delete file from Cloudinary: {}, error: {}", attachment.getFileName(),
						e.getMessage());
			}
		}

		repository.deleteById(messageId);
		log.info("User {} deleted message {} with {} file attachments", userId, messageId, attachments.size());
	}
}
