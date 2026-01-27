package com.example.kns.file.model;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class FileAttachment {
	private Long id;
	private Long messageId;
	private String fileName;
	private String originalFileName;
	private String fileUrl;
	private String fileType;
	private Long fileSize;
	private OffsetDateTime uploadedAt;
	private OffsetDateTime expiresAt;
}
