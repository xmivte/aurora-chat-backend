package com.example.kns.chat.service;

import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import com.example.kns.file.dto.FileMetadataDTO;
import com.example.kns.file.model.FileAttachment;
import com.example.kns.file.repository.FileAttachmentRepository;
import com.example.kns.notifications.NotificationService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.List;

@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
@Service
@RequiredArgsConstructor
@Validated
public class ChatMessageService {

	private final ChatMessagesRepository repository;
	private final NotificationService notificationService;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Transactional
	public ChatMessage saveIncoming(@Valid ChatMessageDTO dto) {
		ChatMessage msg = ChatMessage.builder().senderId(dto.getSenderId()).groupId(dto.getGroupId())
				.content(dto.getContent()).sent(false).build();

		repository.insert(msg);

        if(dto.getFileMetadata() != null && !dto.getFileMetadata().isEmpty()) {
            OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(7);

            for (FileMetadataDTO fileMetadata : dto.getFileMetadata()) {
                FileAttachment fileAttachment = FileAttachment.builder()
                        .messageId(msg.getId())
                        .fileName(fileMetadata.getFileName())
                        .originalFileName(fileMetadata.getOriginalFileName())
                        .fileUrl(fileMetadata.getFileUrl())
                        .fileType(fileMetadata.getFileType())
                        .fileSize(fileMetadata.getFileSize())
                        .expiresAt(expiresAt)
                        .build();

                fileAttachmentRepository.insert(fileAttachment);
            }
        }
		notificationService.onNewMessageSaved(msg.getGroupId(), msg.getSenderId(), msg.getId(), msg.getContent());

		return msg;
	}

	public List<ChatMessage> getAll(@NotBlank String groupId) {
		return repository.findAllMessagesByGroupId(groupId);
	}
}
