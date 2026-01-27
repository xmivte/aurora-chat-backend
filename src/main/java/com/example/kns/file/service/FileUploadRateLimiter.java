package com.example.kns.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class FileUploadRateLimiter {

	private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

	private static final int MAX_UPLOADS_PER_WINDOW = 20;
	private static final Duration TIME_WINDOW = Duration.ofMinutes(1);

	public boolean allowUpload(String userId) {
		if (userId == null || userId.isBlank()) {
			log.warn("Rate limiter called with null/blank userId");
			return true;
		}

		Instant now = Instant.now();

		final boolean[] allowed = {false};

		rateLimitMap.compute(userId, (key, existing) -> {
			if (existing == null) {
				allowed[0] = true;
				return new RateLimitInfo(now, 1);
			}

			if (Duration.between(existing.windowStart, now).compareTo(TIME_WINDOW) > 0) {
				log.debug("Rate limit window reset for user: {}", userId);
				allowed[0] = true;
				return new RateLimitInfo(now, 1);
			}

			if (existing.uploadCount >= MAX_UPLOADS_PER_WINDOW) {
				allowed[0] = false;
				return existing;
			}

			existing.uploadCount++;
			allowed[0] = true;
			return existing;
		});

		if (!allowed[0]) {
			RateLimitInfo info = rateLimitMap.get(userId);
			int count = info != null ? info.uploadCount : 0;
			log.warn("Rate limit exceeded for user: {}. Uploads: {}/{} in current window", userId, count,
					MAX_UPLOADS_PER_WINDOW);
		} else {
			RateLimitInfo info = rateLimitMap.get(userId);
			int count = info != null ? info.uploadCount : 0;
			log.debug("Upload allowed for user: {}. Count: {}/{}", userId, count, MAX_UPLOADS_PER_WINDOW);
		}

		return allowed[0];
	}

	public void resetUserLimit(String userId) {
		rateLimitMap.remove(userId);
		log.info("Reset rate limit for user: {}", userId);
	}

	@Scheduled(fixedRate = 300000)
	public void cleanupExpiredEntries() {
		Instant now = Instant.now();
		int initialSize = rateLimitMap.size();

		rateLimitMap.entrySet().removeIf(entry -> {
			Duration age = Duration.between(entry.getValue().windowStart, now);
			return age.compareTo(TIME_WINDOW.multipliedBy(2)) > 0; // Keep 2x window for safety
		});

		int removed = initialSize - rateLimitMap.size();
		if (removed > 0) {
			log.debug("Cleaned up {} expired rate limit entries", removed);
		}
	}

	public int getCurrentUploadCount(String userId) {
		RateLimitInfo info = rateLimitMap.get(userId);
		if (info == null)
			return 0;

		Instant now = Instant.now();
		if (Duration.between(info.windowStart, now).compareTo(TIME_WINDOW) > 0) {
			return 0;
		}

		return info.uploadCount;
	}

	private static class RateLimitInfo {
		Instant windowStart;
		int uploadCount;

		RateLimitInfo(Instant windowStart, int uploadCount) {
			this.windowStart = windowStart;
			this.uploadCount = uploadCount;
		}
	}
}
