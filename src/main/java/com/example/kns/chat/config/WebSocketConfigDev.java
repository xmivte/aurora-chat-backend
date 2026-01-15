package com.example.kns.chat.config;

import com.example.kns.config.FirebaseHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Profile("local")
@RequiredArgsConstructor
public class WebSocketConfigDev implements WebSocketMessageBrokerConfigurer {

	@Value("${app.frontend.origin}")
	private String frontendOrigin;

	private final FirebaseHandshakeInterceptor firebaseHandshakeInterceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {

		var handler = new DefaultHandshakeHandler() {
			@Override
			protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
					Map<String, Object> attributes) {

				Object p = attributes.get("principal");
				if (p instanceof Principal principal) {
					return principal;
				}
				return request.getPrincipal();
			}
		};

		// FE SockJS endpoint
		registry.addEndpoint("/ws").setAllowedOrigins(frontendOrigin).addInterceptors(firebaseHandshakeInterceptor)
				.setHandshakeHandler(handler).withSockJS();

		// Postman / raw WS testing endpoint
		registry.addEndpoint("/ws-stomp").setAllowedOrigins(frontendOrigin)
				.addInterceptors(firebaseHandshakeInterceptor).setHandshakeHandler(handler);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}
}
