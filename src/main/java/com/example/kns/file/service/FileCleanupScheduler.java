package com.example.kns.file.service;

import com.example.kns.file.config.FileStorageProperties;
import com.example.kns.file.model.FileAttachment;
import com.example.kns.file.repository.FileAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileCleanupScheduler {

	private final FileAttachmentRepository fileRepository;
	private final FileStorageService fileStorageService;
	private final FileStorageProperties config;

	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void cleanupExpiredFiles() {
		log.info("Starting scheduled file cleanup task");

		int cleanupDays = config.getCleanupDays();
		LocalDateTime expirationDate = LocalDateTime.now().minusDays(cleanupDays);
		List<FileAttachment> expiredFiles = fileRepository.findByUploadedAtBefore(expirationDate);

		log.info("Found {} expired files (older than {} days)", expiredFiles.size(), cleanupDays);

		int successCount = 0;
		int failCount = 0;

		for (FileAttachment file : expiredFiles) {
			try {
				fileStorageService.deleteFile(file.getFileName());
				log.debug("Deleted physical file from Cloudinary: {}", file.getFileName());

				fileRepository.deleteById(file.getId());
				successCount++;
				log.debug("Deleted file record: id={}, originalName={}", file.getId(), file.getOriginalFileName());

			} catch (IOException e) {
				failCount++;
				log.error("Failed to delete physical file from Cloudinary: id={}, storedName={}, error={}",
						file.getId(), file.getFileName(), e.getMessage());
			} catch (Exception e) {
				failCount++;
				log.error("Failed to delete file record: id={}, error={}", file.getId(), e.getMessage());
			}
		}

		log.info("File cleanup task completed. Success: {}, Failed: {}", successCount, failCount);
	}

	@Transactional
	public int cleanupFilesOlderThan(int days) {
		log.info("Manual file cleanup initiated for files older than {} days", days);

		LocalDateTime expirationDate = LocalDateTime.now().minusDays(days);
		List<FileAttachment> expiredFiles = fileRepository.findByUploadedAtBefore(expirationDate);

		int deletedCount = 0;
		for (FileAttachment file : expiredFiles) {
			try {
				fileStorageService.deleteFile(file.getFileName());
				fileRepository.deleteById(file.getId());
				deletedCount++;
			} catch (Exception e) {
				log.error("Failed to delete file during manual cleanup: id={}, error={}", file.getId(), e.getMessage());
			}
		}

		log.info("Manual cleanup completed. Deleted {} files", deletedCount);
		return deletedCount;
	}
}
