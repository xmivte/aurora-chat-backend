package com.example.kns.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	// SUDARO STOMP WS KANALUS
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// Front-endui SockJS endpointas
		registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
		// POSTMAN testing endpoint
		registry.addEndpoint("/ws-stomp").setAllowedOrigins("*");
	}

	// Brokerio konfiguracija
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue"); // Top-level destinations
		registry.setApplicationDestinationPrefixes("/app"); // Client sends messages here
		registry.setUserDestinationPrefix("/user"); // private messaging
	}
}
