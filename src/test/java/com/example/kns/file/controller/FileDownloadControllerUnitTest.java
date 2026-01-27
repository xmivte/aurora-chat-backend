package com.example.kns.file.controller;

import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import com.example.kns.dto.UserContext;
import com.example.kns.file.model.FileAttachment;
import com.example.kns.file.repository.FileAttachmentRepository;
import com.example.kns.group.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDownloadControllerUnitTest {

	@Mock
	private FileAttachmentRepository fileAttachmentRepository;

	@Mock
	private ChatMessagesRepository chatMessagesRepository;

	@Mock
	private GroupRepository groupRepository;

	@InjectMocks
	private FileDownloadController controller;

	@Test
	void downloadFile_fileNotFound_returns404() {
		UserContext userContext = new UserContext("user1");
		when(fileAttachmentRepository.findById(1L)).thenReturn(null);

		ResponseEntity<?> response = controller.downloadFile(1L, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isEqualTo("File not found");
	}

	@Test
	void downloadFile_messageNotFound_returns404() {
		UserContext userContext = new UserContext("user1");

		FileAttachment fileAttachment = FileAttachment.builder().id(1L).messageId(100L).fileName("test.txt")
				.originalFileName("test.txt").fileUrl("https://cloudinary.com/test.txt").fileType("text/plain")
				.fileSize(100L).uploadedAt(OffsetDateTime.now()).expiresAt(OffsetDateTime.now().plusDays(7)).build();

		when(fileAttachmentRepository.findById(1L)).thenReturn(fileAttachment);
		when(chatMessagesRepository.findById(100L)).thenReturn(null);

		ResponseEntity<?> response = controller.downloadFile(1L, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isEqualTo("Message not found");
	}

	@Test
	void downloadFile_userNotInGroup_returns403() {
		UserContext userContext = new UserContext("user1");

		FileAttachment fileAttachment = FileAttachment.builder().id(1L).messageId(100L).fileName("test.txt")
				.originalFileName("test.txt").fileUrl("https://cloudinary.com/test.txt").fileType("text/plain")
				.fileSize(100L).uploadedAt(OffsetDateTime.now()).expiresAt(OffsetDateTime.now().plusDays(7)).build();

		ChatMessage message = ChatMessage.builder().id(100L).senderId("sender1").groupId("group1")
				.content("Test message").build();

		when(fileAttachmentRepository.findById(1L)).thenReturn(fileAttachment);
		when(chatMessagesRepository.findById(100L)).thenReturn(message);
		when(groupRepository.isUserInGroup("user1", "group1")).thenReturn(false);

		ResponseEntity<?> response = controller.downloadFile(1L, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getBody()).isEqualTo("You do not have permission to access this file");
	}

	@Test
	void downloadFile_cloudinaryUnavailable_returns500() {
		UserContext userContext = new UserContext("user1");

		FileAttachment fileAttachment = FileAttachment.builder().id(1L).messageId(100L).fileName("test.txt")
				.originalFileName("test.txt").fileUrl("https://invalid-cloudinary-url.invalid/test.txt")
				.fileType("text/plain").fileSize(100L).uploadedAt(OffsetDateTime.now())
				.expiresAt(OffsetDateTime.now().plusDays(7)).build();

		ChatMessage message = ChatMessage.builder().id(100L).senderId("sender1").groupId("group1")
				.content("Test message").build();

		when(fileAttachmentRepository.findById(1L)).thenReturn(fileAttachment);
		when(chatMessagesRepository.findById(100L)).thenReturn(message);
		when(groupRepository.isUserInGroup("user1", "group1")).thenReturn(true);

		ResponseEntity<?> response = controller.downloadFile(1L, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isEqualTo("Failed to download file. Please try again.");
	}
}
