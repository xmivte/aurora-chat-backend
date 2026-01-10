package com.example.kns.file.service;

import com.example.kns.file.dto.FileMetadataDTO;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final List<String> BLOCKED_EXTENSIONS = Arrays.asList(
            ".exe", ".bat", ".sh", ".dll", ".msi", ".app", ".deb", ".rpm",
            ".cmd", ".com", ".scr", ".vbs", ".jar", ".ps1"
    );

    public FileMetadataDTO uploadFile(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of 10 MB");
        }

        String originalFileName = file.getOriginalFilename();

        if(isExecutableFile(originalFileName)) {
            throw new IllegalArgumentException("Executable files are not allowed");
        }

        String uniqueFileName = generateUniqueFileName(originalFileName);

        Storage storage = StorageOptions.getDefaultInstance().getService();

        BlobId blobId = BlobId.of(bucketName, "chat-files/" + uniqueFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());

        String fileUrl = String.format("https://storage.googleapis.com/%s/%s",
                bucketName, blob.getName());

        return FileMetadataDTO.builder()
                .fileUrl(fileUrl)
                .fileName(uniqueFileName)
                .originalFileName(originalFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
    }

    public boolean deleteFile(String fileName) {
        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            BlobId blobId = BlobId.of(bucketName, "chat-files/" + fileName);
            return storage.delete(blobId);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExecutableFile(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return BLOCKED_EXTENSIONS.contains(extension);
    }

    private String generateUniqueFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return timestamp + "-" + uuid + extension;
    }
}
