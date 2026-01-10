package com.example.kns.file.controller;

import com.example.kns.file.dto.FileMetadataDTO;
import com.example.kns.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    private static final long MAX_TOTAL_SIZE = 10 * 1024 * 1024; // 10MB total

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("No files provided");
            }

            long totalSize = 0;
            for (MultipartFile file : files) {
                totalSize += file.getSize();
            }

            if (totalSize > MAX_TOTAL_SIZE) {
                return ResponseEntity.badRequest()
                        .body("Total file size exceeds 10MB limit. Current total: "
                                + (totalSize / 1024 / 1024) + "MB");
            }

            List<FileMetadataDTO> uploadedFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                FileMetadataDTO metadata = fileStorageService.uploadFile(file);
                uploadedFiles.add(metadata);
            }

            return ResponseEntity.ok(uploadedFiles);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload files: " + e.getMessage());
        }
    }
}
