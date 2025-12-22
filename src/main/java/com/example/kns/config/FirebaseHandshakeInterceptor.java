package com.example.kns.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServletServerHttpRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Map;
import java.util.List;

@Component
@AllArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI bean")
public class FirebaseHandshakeInterceptor implements HandshakeInterceptor {

	private final FirebaseAuth firebaseAuth;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) {

		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpServletRequest req = servletRequest.getServletRequest();

			String token = req.getParameter("token");
			if (token == null || token.isEmpty()) {
				return false; // reject unauthenticated WS handshake
			}

			try {
				FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);

				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						decodedToken.getUid(), null, List.of());

				attributes.put("principal", auth);
			} catch (FirebaseAuthException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception ex) {

	}
}
