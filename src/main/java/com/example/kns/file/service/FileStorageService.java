package com.example.kns.file.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.kns.file.dto.FileMetadataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${cloudinary.credentials}")
    private String credentialsPath;

    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> BLOCKED_EXTENSIONS = Arrays.asList(
            ".exe", ".bat", ".sh", ".dll", ".msi", ".app", ".deb", ".rpm",
            ".cmd", ".com", ".scr", ".vbs", ".jar", ".ps1"
    );

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> credentials = mapper.readValue(
                new FileInputStream(credentialsPath),
                Map.class
        );

        // Initialize Cloudinary
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", credentials.get("cloud_name"),
                "api_key", credentials.get("api_key"),
                "api_secret", credentials.get("api_secret")
        ));
    }

    public FileMetadataDTO uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10 MB limit");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("File name is missing");
        }

        String lowerFileName = originalFileName.toLowerCase();
        for (String ext : BLOCKED_EXTENSIONS) {
            if (lowerFileName.endsWith(ext)) {
                throw new IllegalArgumentException("Executable files are not allowed");
            }
        }

        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "chat-files",
                "public_id", uniqueFileName,
                "resource_type", "auto" // auto-detect file type
        ));

        String fileUrl = (String) uploadResult.get("secure_url");

        return FileMetadataDTO.builder()
                .fileUrl(fileUrl)
                .fileName(uniqueFileName)
                .originalFileName(originalFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
    }

    public void deleteFile(String fileName) throws IOException {
        String publicId = "chat-files/" + fileName;
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
