package com.example.kns.file.controller;

import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.repository.ChatMessagesRepository;
import com.example.kns.file.model.FileAttachment;
import com.example.kns.file.repository.FileAttachmentRepository;
import com.example.kns.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileDownloadController {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    private final GroupRepository groupRepository;

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long fileId, Authentication authentication) {
        String userId = authentication.getName();

        FileAttachment fileAttachment = fileAttachmentRepository.findById(fileId);
        if (fileAttachment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File not found");
        }

        ChatMessage message = chatMessagesRepository.findById(fileAttachment.getMessageId());
        if(message == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Message not found");
        }

        boolean isUserInGroup = groupRepository.isUserInGroup(userId, message.getGroupId());
        if (!isUserInGroup) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to access this file");
        }

        // Browser will be redirected to the file URL
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", fileAttachment.getFileUrl())
                .build();
    }
}
