package com.example.kns.file.controller;

import com.example.kns.dto.UserContext;
import com.example.kns.file.config.FileStorageProperties;
import com.example.kns.file.dto.FileMetadataDTO;
import com.example.kns.file.service.FileStorageService;
import com.example.kns.file.service.FileUploadRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

	private final FileStorageService fileStorageService;
	private final FileUploadRateLimiter rateLimiter;
	private final FileStorageProperties config;

	@PostMapping("/upload")
	public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files,
			@AuthenticationPrincipal UserContext userContext) {
		try {
			String userId = userContext != null ? userContext.getEmail() : "anonymous";
			if (!rateLimiter.allowUpload(userId)) {
				log.warn("Rate limit exceeded for user: {}", userId);
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
						.body("Too many upload requests. Please try again later.");
			}

			if (files == null || files.length == 0) {
				return ResponseEntity.badRequest().body("No files provided");
			}

			int maxFiles = config.getMaxFilesPerMessage();
			if (files.length > maxFiles) {
				return ResponseEntity.badRequest().body("Maximum " + maxFiles + " files allowed per message");
			}

			long totalSize = 0;
			for (MultipartFile file : files) {
				totalSize += file.getSize();
			}

			long maxTotalSize = config.getMaxFileSize();
			if (totalSize > maxTotalSize) {
				long maxSizeMB = maxTotalSize / (1024 * 1024);
				return ResponseEntity.badRequest().body("Total file size exceeds " + maxSizeMB
						+ "MB limit. Current total: " + (totalSize / 1024 / 1024) + "MB");
			}

			List<FileMetadataDTO> uploadedFiles = new ArrayList<>();
			for (MultipartFile file : files) {
				FileMetadataDTO metadata = fileStorageService.uploadFile(file);
				uploadedFiles.add(metadata);
			}

			log.info("User {} uploaded {} files (total size: {} bytes)", userId, files.length, totalSize);

			return ResponseEntity.ok(uploadedFiles);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to upload files. Please try again.");
		}
	}
}
