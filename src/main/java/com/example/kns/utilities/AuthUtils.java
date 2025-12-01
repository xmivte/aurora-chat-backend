package com.example.kns.utilities;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthUtils {

    public Optional<String> getHeadersValue(HttpServletRequest request, String headersName) {
        if(request == null){
            return Optional.empty();
        }

        if(headersName == null || headersName.isEmpty()) {
            return Optional.empty();
        }

        var token = request.getHeader(headersName);

        if(token == null || !token.startsWith("Bearer ")){
            return Optional.empty();
        }

        var jwt = token.substring(7);
        return Optional.of(jwt);
    }
}
