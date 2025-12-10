package com.example.kns.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/secure").authenticated().anyRequest().permitAll())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(firebaseJwtDecoder())));
		return httpSecurity.build();
	}

	@Bean
	public JwtDecoder firebaseJwtDecoder() {
		return token -> {
			try {
				FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
				Map<String, Object> claims = decodedToken.getClaims();

				Instant issuedAt = Instant.ofEpochSecond(((Number) claims.getOrDefault("iat", 0)).longValue());
				Instant expiresAt = Instant.ofEpochSecond(((Number) claims.getOrDefault("exp", 0)).longValue());

				return new Jwt(token, issuedAt, expiresAt, Map.of("alg", "RS256"), claims);
			} catch (FirebaseAuthException e) {
				throw new JwtException("Invalid Firebase ID token", e);
			}
		};
	}
}
