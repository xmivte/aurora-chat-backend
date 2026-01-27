package com.example.kns.file.controller;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.file.service.FileStorageService;
import com.example.kns.file.service.FileUploadRateLimiter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@Import(TestEmbeddedPostgresConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FileUploadControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FirebaseAuth firebaseAuth;

	@MockBean
	private FileStorageService fileStorageService;

	@Autowired
	private FileUploadRateLimiter rateLimiter;

	@BeforeEach
	void setUp() throws Exception {
		long iat = Instant.now().getEpochSecond();
		long exp = iat + 3600;

		FirebaseToken token = mock(FirebaseToken.class);
		when(token.getUid()).thenReturn("testUser");
		when(token.getClaims()).thenReturn(Map.of("sub", "testUser", "iat", iat, "exp", exp));
		when(firebaseAuth.verifyIdToken(anyString())).thenReturn(token);

		rateLimiter.resetUserLimit("testUser");

		when(fileStorageService.uploadFile(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
			org.springframework.web.multipart.MultipartFile file = invocation.getArgument(0);
			return com.example.kns.file.dto.FileMetadataDTO.builder().fileName(file.getOriginalFilename())
					.originalFileName(file.getOriginalFilename())
					.fileUrl("https://cloudinary.com/" + file.getOriginalFilename()).fileType(file.getContentType())
					.fileSize(file.getSize()).build();
		});
	}

	@Test
	void uploadFiles_noAuthorizationHeader_returnsUnauthorized() throws Exception {
		MockMultipartFile file = new MockMultipartFile("files", "test.txt", MediaType.TEXT_PLAIN_VALUE,
				"test content".getBytes());

		mockMvc.perform(multipart("/api/files/upload").file(file)).andExpect(status().isUnauthorized());
	}

	@Test
	void uploadFiles_tooManyFiles_returnsBadRequest() throws Exception {
		MockMultipartFile file = new MockMultipartFile("files", "test.txt", MediaType.TEXT_PLAIN_VALUE,
				"test content".getBytes());

		mockMvc.perform(multipart("/api/files/upload").file(file).file(file).file(file).file(file).file(file).file(file)
				.header("Authorization", "Bearer fake-token")).andExpect(status().isBadRequest())
				.andExpect(content().string("Maximum 5 files allowed per message"));
	}

	@Test
	void uploadFiles_fileTooLarge_returnsBadRequest() throws Exception {
		byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
		MockMultipartFile file = new MockMultipartFile("files", "large.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE,
				largeContent);

		mockMvc.perform(multipart("/api/files/upload").file(file).header("Authorization", "Bearer fake-token"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("exceeds 10MB limit")));
	}

	@Test
	void uploadFiles_rateLimitExceeded_returns429() throws Exception {
		MockMultipartFile file = new MockMultipartFile("files", "test.txt", MediaType.TEXT_PLAIN_VALUE,
				"test content".getBytes());

		for (int i = 0; i < 20; i++) {
			mockMvc.perform(multipart("/api/files/upload").file(file).header("Authorization", "Bearer fake-token"))
					.andExpect(status().isOk());
		}

		mockMvc.perform(multipart("/api/files/upload").file(file).header("Authorization", "Bearer fake-token"))
				.andExpect(status().isTooManyRequests())
				.andExpect(content().string("Too many upload requests. Please try again later."));
	}

	@Test
	void uploadFiles_multipleFilesTotalSizeExceeds10MB_returnsBadRequest() throws Exception {
		byte[] content1 = new byte[6 * 1024 * 1024];
		byte[] content2 = new byte[6 * 1024 * 1024];

		MockMultipartFile file1 = new MockMultipartFile("files", "file1.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE,
				content1);
		MockMultipartFile file2 = new MockMultipartFile("files", "file2.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE,
				content2);

		mockMvc.perform(
				multipart("/api/files/upload").file(file1).file(file2).header("Authorization", "Bearer fake-token"))
				.andExpect(status().isBadRequest()).andExpect(
						content().string(org.hamcrest.Matchers.containsString("Total file size exceeds 10MB limit")));
	}

	@Test
	void uploadFiles_validTextFile_returnsOk() throws Exception {
		MockMultipartFile file = new MockMultipartFile("files", "test.txt", MediaType.TEXT_PLAIN_VALUE,
				"test content".getBytes());

		mockMvc.perform(multipart("/api/files/upload").file(file).header("Authorization", "Bearer fake-token"))
				.andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void uploadFiles_multipleValidFiles_returnsOk() throws Exception {
		MockMultipartFile file1 = new MockMultipartFile("files", "test1.txt", MediaType.TEXT_PLAIN_VALUE,
				"test content 1".getBytes());
		MockMultipartFile file2 = new MockMultipartFile("files", "test2.txt", MediaType.TEXT_PLAIN_VALUE,
				"test content 2".getBytes());
		MockMultipartFile file3 = new MockMultipartFile("files", "test3.txt", MediaType.TEXT_PLAIN_VALUE,
				"test content 3".getBytes());

		mockMvc.perform(multipart("/api/files/upload").file(file1).file(file2).file(file3).header("Authorization",
				"Bearer fake-token")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	void uploadFiles_validImageFile_returnsOk() throws Exception {
		byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
		MockMultipartFile file = new MockMultipartFile("files", "image.png", "image/png", pngHeader);

		mockMvc.perform(multipart("/api/files/upload").file(file).header("Authorization", "Bearer fake-token"))
				.andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(1));
	}
}
