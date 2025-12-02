package com.example.kns.controllers;

import com.example.kns.config.TestEmbeddedPostgresConfig;
import com.example.kns.chat.dto.ChatMessageDTO;
import com.example.kns.chat.model.ChatMessage;
import com.example.kns.chat.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestEmbeddedPostgresConfig.class)
@ActiveProfiles("test")
@TestPropertySource("classpath:application-test.yml")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebSocketIntegrationTest {

	@LocalServerPort
	int port;

	@Autowired
	ChatMessageService messageService;

	private WebSocketStompClient stomp = null;

	@BeforeEach
	void setup() {
		List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
		SockJsClient sockJs = new SockJsClient(transports);
		this.stomp = new WebSocketStompClient(sockJs);
		this.stomp.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@Test
	void sendMessage_shouldBeDeliveredToSubscriber() throws Exception {

		CountDownLatch latch = new CountDownLatch(1);
		List<Map<String, Object>> received = new ArrayList<>();

		String wsUrl = "ws://localhost:" + port + "/ws";
		StompSession session = stomp.connect(wsUrl, new WebSocketHttpHeaders(), new StompSessionHandlerAdapter() {
		}).get(3, TimeUnit.SECONDS);

		session.subscribe("/topic/chat.room1", new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return Map.class; // JSON decoded
			}
			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				received.add((Map<String, Object>) payload);
				latch.countDown();
			}
		});

		ChatMessageDTO dto = new ChatMessageDTO();
		dto.setSenderId(5L);
		dto.setGroupId("room1");
		dto.setContent("hello");
		session.send("/app/send.message", dto);

		messageService.pollMessagesAndBroadcast();

		boolean ok = latch.await(1, TimeUnit.SECONDS);
		assertThat(ok).isTrue();

		Map<String, Object> msgMap = received.get(0);
		ChatMessage msg = ChatMessage.builder().content((String) msgMap.get("content"))
				.groupId((String) msgMap.get("groupId")).senderId(((Number) msgMap.get("senderId")).longValue())
				.build();

		assertThat(msg.getContent()).isEqualTo("hello");
		assertThat(msg.getGroupId()).isEqualTo("room1");
		assertThat(msg.getSenderId()).isEqualTo(5L);
	}
}