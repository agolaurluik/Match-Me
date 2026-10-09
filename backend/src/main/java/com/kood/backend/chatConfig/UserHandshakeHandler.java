package com.kood.backend.chatConfig;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {
        Long userId = (Long) attributes.get("userId");
        if (userId == null) {

            return null;
        }
        return new Principal() {
            @Override
            public String getName() {
                return String.valueOf(userId);
            }
        };
    }
}
