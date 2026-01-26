package com.example.kns.file.controller;

import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import com.example.kns.dto.UserContext;
import com.example.kns.file.model.FileAttachment;
import com.example.kns.file.repository.FileAttachmentRepository;
import com.example.kns.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileDownloadController {

	private final FileAttachmentRepository fileAttachmentRepository;
	private final ChatMessagesRepository chatMessagesRepository;
	private final GroupRepository groupRepository;

	@GetMapping("/download/{fileId}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> downloadFile(@PathVariable Long fileId, @AuthenticationPrincipal UserContext userContext) {
		String userId = userContext.getEmail();

		FileAttachment fileAttachment = fileAttachmentRepository.findById(fileId);
		if (fileAttachment == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
		}

		ChatMessage message = chatMessagesRepository.findById(fileAttachment.getMessageId());
		if (message == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
		}

		boolean isUserInGroup = groupRepository.isUserInGroup(userId, message.getGroupId());
		if (!isUserInGroup) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to access this file");
		}

		try {
			String fileUrl = fileAttachment.getFileUrl();
			String contentType = fileAttachment.getFileType();

			URL url = new URL(fileUrl);
			InputStream inputStream = url.openStream();
			Resource resource = new InputStreamResource(inputStream);

			String originalFileName = fileAttachment.getOriginalFileName();
			String encodedFilename = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8).replaceAll("\\+",
					"%20");

			String contentDisposition = String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
					originalFileName, encodedFilename);

			log.info("User {} downloaded file {} (fileId: {}, messageId: {}, groupId: {}, size: {} bytes)", userId,
					originalFileName, fileId, message.getId(), message.getGroupId(), fileAttachment.getFileSize());

			return ResponseEntity.ok()
					.contentType(
							MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
					.contentLength(fileAttachment.getFileSize())
					.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(resource);
		} catch (IOException e) {
			log.error("Failed to download file from Cloudinary: fileId={}, error={}", fileId, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to download file. Please try again.");
		}
	}
}
