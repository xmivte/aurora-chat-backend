package com.example.kns.file.repository;

import com.example.kns.file.model.FileAttachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileAttachmentRepository {
    @Insert("""
            INSERT INTO db.file_attachments(message_id, file_name, original_file_name,
                                            file_url, file_type, file_size, expires_at)
            VALUES (#{messageId}, #{fileName}, #{originalFileName},
                    #{fileUrl}, #{fileType}, #{fileSize}, #{expiresAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FileAttachment fileAttachment);

    @Select("""
            SELECT id, message_id AS messageId, file_name AS fileName,
                   original_file_name AS originalFileName, file_url AS fileUrl,
                    file_type AS fileType, file_size AS fileSize,
                    uploaded_at AS uploadedAt, expires_at AS expiresAt
            FROM db.file_attachments
            WHERE message_id = #{messageId}
            ORDER BY uploaded_at ASC
            """)
    List<FileAttachment> findByMessageId(@Param("messageId") Long messageId);
}
