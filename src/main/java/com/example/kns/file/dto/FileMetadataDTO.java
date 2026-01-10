package com.example.kns.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataDTO {

    @NotBlank(message = "File URL is required")
    private String fileURL;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "Original file name is required")
    private String originalFileName;

    @NotBlank(message = "File type is required")
    private String fileType;

    @NotNull(message = "File size is required")
    private Long fileSize;

    // Time data when fetching file
    private OffsetDateTime uploadedAt;
    private OffsetDateTime expiresAt;
}
