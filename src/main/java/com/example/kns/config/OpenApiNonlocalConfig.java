package com.example.kns.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

@Configuration
@Profile("!local")
public class OpenApiNonlocalConfig {

	@Bean
	public OpenApiCustomizer nonlocalOpenApiCustomizer() {
		return openApi -> openApi
				.setServers(Collections.singletonList(new Server().url("https://aurora-chat.api.devbstaging.com")));
	}
}
