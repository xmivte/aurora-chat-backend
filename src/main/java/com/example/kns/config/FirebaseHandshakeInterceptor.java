package com.example.kns.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class FirebaseHandshakeInterceptor implements HandshakeInterceptor {

	public static final String ATTR_PRINCIPAL = "principal";

	private final FirebaseAuth firebaseAuth;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) {

		if (!(request instanceof ServletServerHttpRequest servletRequest)) {
			return false;
		}

		HttpServletRequest req = servletRequest.getServletRequest();
		String token = req.getParameter("token");

		if (token == null || token.isBlank()) {
			return false;
		}

		try {
			FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
			String uid = decoded.getUid();

			attributes.put(ATTR_PRINCIPAL, (Principal) () -> uid);

			return true;
		} catch (FirebaseAuthException e) {
			return false;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception ex) {
		// no-op
	}
}
