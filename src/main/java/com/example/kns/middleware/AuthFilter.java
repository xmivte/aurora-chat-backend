package com.example.kns.middleware;

import com.example.kns.utilities.AuthUtils;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final AuthUtils authUtils;

    public enum Role {
        User, Guest
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var jwt = authUtils.getHeadersValue(request, "Authorization");

        if(jwt.isPresent()) {
            try {
                var token = jwt.get().substring(7);
                var firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token, true);

                var principal = firebaseToken.getUid();
                String credentials = null;
                var authorities = List.of(new SimpleGrantedAuthority(Role.User.name()));

                var authContext = new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
                SecurityContextHolder.getContext().setAuthentication(authContext);

            } catch (FirebaseAuthException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else{

            var principal = Role.Guest.name();
            String credentials = null;
            var authorities = List.of(new SimpleGrantedAuthority(Role.Guest.name()));

            var authContext = new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
            SecurityContextHolder.getContext().setAuthentication(authContext);
        }

        filterChain.doFilter(request, response);
    }
}
