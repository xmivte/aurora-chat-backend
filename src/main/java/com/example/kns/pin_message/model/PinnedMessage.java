package com.example.kns.pin_message.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
public class PinnedMessage {
	private Long id;
	private Long messageId;
	private String groupId;
	private String pinnedBy;
	private OffsetDateTime pinnedAt;
}