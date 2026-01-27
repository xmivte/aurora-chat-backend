package com.example.kns.file.controller;

import com.example.kns.dto.UserContext;
import com.example.kns.file.config.FileStorageProperties;
import com.example.kns.file.dto.FileMetadataDTO;
import com.example.kns.file.service.FileStorageService;
import com.example.kns.file.service.FileUploadRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerUnitTest {

	@Mock
	private FileStorageService fileStorageService;

	@Mock
	private FileUploadRateLimiter rateLimiter;

	@Mock
	private FileStorageProperties config;

	@InjectMocks
	private FileUploadController controller;

	@BeforeEach
	void setUp() {
		lenient().when(config.getMaxFilesPerMessage()).thenReturn(5);
		lenient().when(config.getMaxFileSize()).thenReturn(10485760L); // 10MB
	}

	@Test
	void uploadFiles_rateLimitExceeded_returns429() throws IOException {
		UserContext userContext = new UserContext("user1");
		when(rateLimiter.allowUpload("user1")).thenReturn(false);

		MultipartFile file = mock(MultipartFile.class);
		MultipartFile[] files = new MultipartFile[]{file};

		ResponseEntity<?> response = controller.uploadFiles(files, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(response.getBody()).isEqualTo("Too many upload requests. Please try again later.");
		verify(rateLimiter).allowUpload("user1");
		verify(fileStorageService, never()).uploadFile(any());
	}

	@Test
	void uploadFiles_noAuthentication_usesAnonymousUser() throws IOException {
		when(rateLimiter.allowUpload("anonymous")).thenReturn(false);

		MultipartFile file = mock(MultipartFile.class);
		MultipartFile[] files = new MultipartFile[] {file};

		ResponseEntity<?> response = controller.uploadFiles(files, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		verify(rateLimiter).allowUpload("anonymous");
	}

	@Test
	void uploadFiles_noFiles_returnsBadRequest() throws IOException {
		UserContext userContext = new UserContext("user1");
		when(rateLimiter.allowUpload("user1")).thenReturn(true);

		MultipartFile[] files = new MultipartFile[]{};

		ResponseEntity<?> response = controller.uploadFiles(files, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo("No files provided");
		verify(fileStorageService, never()).uploadFile(any());
	}

	@Test
	void uploadFiles_tooManyFiles_returnsBadRequest() throws IOException {
		UserContext userContext = new UserContext("user1");
		when(rateLimiter.allowUpload("user1")).thenReturn(true);

		MultipartFile file = mock(MultipartFile.class);
		MultipartFile[] files = new MultipartFile[]{file, file, file, file, file, file};

		ResponseEntity<?> response = controller.uploadFiles(files, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).asString().contains("Maximum 5 files allowed");
		verify(fileStorageService, never()).uploadFile(any());
	}

	@Test
	void uploadFiles_totalSizeExceeds10MB_returnsBadRequest() throws IOException {
		UserContext userContext = new UserContext("user1");
		when(rateLimiter.allowUpload("user1")).thenReturn(true);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(6 * 1024 * 1024L); // 6MB

		MultipartFile[] files = new MultipartFile[]{file, file}; // Total 12MB

		ResponseEntity<?> response = controller.uploadFiles(files, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).asString().contains("Total file size exceeds 10MB limit");
		verify(fileStorageService, never()).uploadFile(any());
	}

	@Test
	void uploadFiles_validRequest_returnsOkWithMetadata() throws IOException {
		UserContext userContext = new UserContext("user1");
		when(rateLimiter.allowUpload("user1")).thenReturn(true);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(1000L);

		FileMetadataDTO metadata = FileMetadataDTO.builder().fileName("test.jpg").originalFileName("test.jpg")
				.fileUrl("https://cloudinary.com/test.jpg").fileType("image/jpeg").fileSize(1000L).build();

		when(fileStorageService.uploadFile(file)).thenReturn(metadata);

		MultipartFile[] files = new MultipartFile[]{file};

		ResponseEntity<?> response = controller.uploadFiles(files, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).asList().hasSize(1);
		verify(fileStorageService).uploadFile(file);
		verify(rateLimiter).allowUpload("user1");
	}

	@Test
	void uploadFiles_illegalArgumentException_returnsBadRequest() throws IOException {
		UserContext userContext = new UserContext("user1");
		when(rateLimiter.allowUpload("user1")).thenReturn(true);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(1000L);

		when(fileStorageService.uploadFile(file))
				.thenThrow(new IllegalArgumentException("Executable files are not allowed"));

		MultipartFile[] files = new MultipartFile[]{file};

		ResponseEntity<?> response = controller.uploadFiles(files, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo("Executable files are not allowed");
	}

	@Test
	void uploadFiles_ioException_returnsInternalServerError() throws IOException {
		UserContext userContext = new UserContext("user1");
		when(rateLimiter.allowUpload("user1")).thenReturn(true);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(1000L);

		when(fileStorageService.uploadFile(file)).thenThrow(new IOException("Network error"));

		MultipartFile[] files = new MultipartFile[]{file};

		ResponseEntity<?> response = controller.uploadFiles(files, userContext);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isEqualTo("Failed to upload files. Please try again.");
	}
}
