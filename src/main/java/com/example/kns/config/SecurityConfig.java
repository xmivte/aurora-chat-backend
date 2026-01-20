package com.example.kns.config;

import com.example.kns.dto.UserContext;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class SecurityConfig {

	@Value("${app.frontend.origin}")
	private String frontendOrigin;

	private final FirebaseAuth firebaseAuth;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/v3/**").permitAll().requestMatchers(HttpMethod.GET, "/ws/**")
						.permitAll().requestMatchers("/ws/**").permitAll().requestMatchers("/ws-stomp/**").permitAll()
						.requestMatchers("/ws-stomp").permitAll().requestMatchers(HttpMethod.GET, "/hello", "/test")
						.permitAll().requestMatchers("/api/groups/*/pinned-messages/**").permitAll().anyRequest()
						.authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(firebaseJwtDecoder())
						.jwtAuthenticationConverter(jwtAuthenticationConverter())));
		return httpSecurity.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(frontendOrigin));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
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
				Map<String, Object> claims = new HashMap<>(decodedToken.getClaims());

				Instant issuedAt = Instant.ofEpochSecond(((Number) claims.getOrDefault("iat", 0)).longValue());
				Instant expiresAt = Instant.ofEpochSecond(((Number) claims.getOrDefault("exp", 0)).longValue());

				claims.put("email", decodedToken.getUid());

				return new Jwt(token, issuedAt, expiresAt, Map.of("alg", "RS256"), claims);
			} catch (FirebaseAuthException e) {
				throw new JwtException("Invalid Firebase ID token", e);
			}
		};
	}

	@Bean
	public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
		return new Converter<Jwt, AbstractAuthenticationToken>() {
			@Override
			public AbstractAuthenticationToken convert(Jwt jwt) {
				final var user_id = jwt.getClaimAsString("email");

				return new UsernamePasswordAuthenticationToken(new UserContext(user_id), jwt,
						List.of(new SimpleGrantedAuthority("ROLE_USER")));
			}
		};
	}
}
