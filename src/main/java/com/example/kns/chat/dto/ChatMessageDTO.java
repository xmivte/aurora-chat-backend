package com.example.kns.chat.dto;

import com.example.kns.file.dto.FileMetadataDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

	private Long id;

	@NotBlank(message = "senderId is blank")
	private String senderId;

	@NotBlank(message = "groupId is blank")
	private String groupId;

	@NotBlank(message = "content is empty")
	@Size(max = 2000, message = "Message is too long (max 2000 chars)")
	private String content;

    @Valid
    private List<FileMetadataDTO> fileMetadata;
    private OffsetDateTime createdAt;
    private String username;

    @AssertTrue(message = "Message must have content or file attachments")
    private boolean isValidMessage() {
        return (content != null && !content.isBlank()) || (fileMetadata != null && !fileMetadata.isEmpty());
    }

    @AssertTrue(message = "Total file size cannot exceed 10 MB")
    private boolean isValidTotalFileSize() {
        if (fileMetadata == null || fileMetadata.isEmpty()) {
            return true;
        }
        long totalSize = fileMetadata.stream().mapToLong(FileMetadataDTO::getFileSize).sum();
        return totalSize <= 10 * 1024 * 1024;
    }
}
