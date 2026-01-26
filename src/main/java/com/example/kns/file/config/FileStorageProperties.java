package com.example.kns.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "file-storage")
public class FileStorageProperties {

	private long maxFileSize = 10485760L;

	private int maxFilesPerMessage = 5;

	private String cloudinaryFolder = "chat-files";

	private int expirationDays = 7;

	private int cleanupDays = 30;

	private List<String> blockedExtensions;

	private int maxFileNameLength = 200;
}
