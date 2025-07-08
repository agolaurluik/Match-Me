package com.kood.backend.chatConfig;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        Long userId = (Long) attributes.get("userId");
        if (userId == null) {
            System.out.println("User ID not found in WebSocket handshake");
            return null;
        }
        System.out.println("determineUser: userId in attributes = " + userId);
        return new Principal() {
            @Override
            public String getName() {
                System.out.println("determineUser: returning Principal name = " + userId);
                return String.valueOf(userId);
            }
        };
    }
}
