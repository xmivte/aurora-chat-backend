package com.example.kns.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("KNS Demo Backend API")
                .version("v0.0.1")
                .description("Backend API for Sourcery Academy demo project")
                .contact(new Contact().name("Team").email("viktorastimtim@gmail.com"))
        );
    }
}
