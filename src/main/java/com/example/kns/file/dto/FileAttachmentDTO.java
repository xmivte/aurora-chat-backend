package com.example.kns.file.dto;

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
public class FileAttachmentDTO {

	private Long id;
	private String fileName;
	private String originalFileName;
	private String fileType;
	private Long fileSize;
	private OffsetDateTime uploadedAt;
	private OffsetDateTime expiresAt;
}
