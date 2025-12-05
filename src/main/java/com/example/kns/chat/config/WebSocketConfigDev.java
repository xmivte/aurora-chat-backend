package com.example.kns.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Profile({"!prod"})
public class WebSocketConfigDev implements WebSocketMessageBrokerConfigurer {

	@Value("${app.frontend.origin}")
	private String frontendOrigin;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// FE SockJS endpoint
		registry.addEndpoint("/ws").setAllowedOrigins(frontendOrigin).withSockJS();
		// POSTMAN testing endpoint
		registry.addEndpoint("/ws-stomp").setAllowedOrigins("*");
	}

	// Message broker configuration points
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic");
		registry.setApplicationDestinationPrefixes("/app");
	}
}
