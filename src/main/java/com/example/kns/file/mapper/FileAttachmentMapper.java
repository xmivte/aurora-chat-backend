package com.example.kns.file.mapper;

import com.example.kns.file.dto.FileAttachmentDTO;
import com.example.kns.file.model.FileAttachment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class FileAttachmentMapper {

	private FileAttachmentMapper() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	public static FileAttachmentDTO toDTO(FileAttachment attachment) {
		if (attachment == null) {
			return null;
		}

		return FileAttachmentDTO.builder().id(attachment.getId()).fileName(attachment.getFileName())
				.originalFileName(attachment.getOriginalFileName()).fileType(attachment.getFileType())
				.fileSize(attachment.getFileSize()).uploadedAt(attachment.getUploadedAt())
				.expiresAt(attachment.getExpiresAt()).build();
	}

	public static List<FileAttachmentDTO> toDTOList(List<FileAttachment> attachments) {
		if (attachments == null || attachments.isEmpty()) {
			return null;
		}

		return attachments.stream().map(FileAttachmentMapper::toDTO).collect(Collectors.toList());
	}

	public static List<FileAttachmentDTO> toDTOListOrEmpty(List<FileAttachment> attachments) {
		if (attachments == null || attachments.isEmpty()) {
			return Collections.emptyList();
		}

		return attachments.stream().map(FileAttachmentMapper::toDTO).collect(Collectors.toList());
	}
}
