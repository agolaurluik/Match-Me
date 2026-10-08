package com.kood.backend.chatConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import com.kood.backend.security.jwtconfig.JwtUtils;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes)
            throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            URI uri = servletRequest.getURI();
            String query = uri.getQuery();

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        String token = param.substring("token=".length());
                        if (jwtUtils.validateJwtToken(token)) {
                            Long userId = jwtUtils.getUserIdFromJwtToken(token);
                            attributes.put("userId", userId);
                            System.out.println(
                                    "HandshakeInterceptor JWT extracted from query string: userId=" + userId);
                            return true;
                        }
                    }
                }
            }
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @Nullable Exception exception) {
        // Does nothing for now
    }
}
