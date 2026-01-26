package com.example.kns.file.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.kns.file.config.FileStorageProperties;
import com.example.kns.file.dto.FileMetadataDTO;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

	private final Cloudinary cloudinary;
	private final FileStorageProperties config;
	private final Tika tika = new Tika();

	public FileMetadataDTO uploadFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("File is empty");
		}

		if (file.getSize() > config.getMaxFileSize()) {
			long maxSizeMB = config.getMaxFileSize() / (1024 * 1024);
			throw new IllegalArgumentException("File size exceeds " + maxSizeMB + " MB limit");
		}

		String originalFileName = file.getOriginalFilename();
		if (originalFileName == null || originalFileName.isBlank()) {
			throw new IllegalArgumentException("File name is missing");
		}

		if (originalFileName.length() > config.getMaxFileNameLength()) {
			throw new IllegalArgumentException(
					"File name is too long. Maximum " + config.getMaxFileNameLength() + " characters allowed");
		}

		String lowerFileName = originalFileName.toLowerCase(java.util.Locale.ROOT);
		for (String ext : config.getBlockedExtensions()) {
			if (lowerFileName.endsWith(ext)) {
				throw new IllegalArgumentException("Executable files are not allowed");
			}
		}

		String detectedMimeType;
		try (InputStream inputStream = file.getInputStream()) {
			detectedMimeType = tika.detect(inputStream, originalFileName);
		}

		if (detectedMimeType != null
				&& (detectedMimeType.contains("executable") || detectedMimeType.contains("x-msdownload")
						|| detectedMimeType.contains("x-msdos-program") || detectedMimeType.contains("x-dosexec")
						|| detectedMimeType.contains("x-sh") || detectedMimeType.contains("x-bat"))) {
			throw new IllegalArgumentException("Executable files are not allowed: " + detectedMimeType);
		}

		String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

		Map<String, Object> uploadResult;
		try {
			uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder",
					config.getCloudinaryFolder(), "public_id", uniqueFileName, "resource_type", "auto"));
		} catch (IOException e) {
			log.error("Cloudinary upload failed - service may be unavailable: {}", e.getMessage());
			throw new IOException("File storage service is currently unavailable. Please try again later.", e);
		}

		String fileUrl = (String) uploadResult.get("secure_url");

		return FileMetadataDTO.builder().fileUrl(fileUrl).fileName(uniqueFileName).originalFileName(originalFileName)
				.fileType(file.getContentType()).fileSize(file.getSize()).build();
	}

	public void deleteFile(String fileName) throws IOException {
		String publicId = config.getCloudinaryFolder() + "/" + fileName;
		try {
			cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
		} catch (IOException e) {
			log.error("Cloudinary delete failed for file {} - service may be unavailable: {}", fileName,
					e.getMessage());
			throw new IOException("File storage service is currently unavailable. Please try again later.", e);
		}
	}

	public Path getUploadDirectory() {
		return Paths.get(config.getCloudinaryFolder());
	}
}
