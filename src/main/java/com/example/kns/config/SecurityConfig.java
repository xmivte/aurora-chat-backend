package com.example.kns.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final FirebaseAuth firebaseAuth;

	@Value("${app.frontend.origin}")
	private String frontendOrigin;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/v3/**").permitAll()

						// ✅ allow websocket endpoints
						.requestMatchers("/ws/**").permitAll()
						.requestMatchers("/ws-stomp/**").permitAll()
						.requestMatchers("/ws-stomp").permitAll()

						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(firebaseJwtDecoder())));

		return httpSecurity.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(frontendOrigin));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public JwtDecoder firebaseJwtDecoder() {
		return token -> {
			try {
				FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
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
