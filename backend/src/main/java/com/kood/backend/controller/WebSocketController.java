package com.kood.backend.controller;

import com.kood.backend.dto.ChatDTOs.ChatMessageDTO;
import com.kood.backend.dto.ChatDTOs.SendMessageDTO;
import com.kood.backend.dto.ChatDTOs.TypingStatus;
import com.kood.backend.dto.ConnectionDTOs.ConnectionUpdateNotification;
import com.kood.backend.dto.ConnectionDTOs.OnlineStatusCheck;
import com.kood.backend.dto.ConnectionDTOs.UserStatusCheck;
import com.kood.backend.entity.ChatEntities.ChatMessage;
import com.kood.backend.entity.ConnectionEntities.Connection;
import com.kood.backend.exceptions.NotFoundException;
import com.kood.backend.mapper.ChatMessageMapper;
import com.kood.backend.mapper.ConnectionMapper;
import com.kood.backend.service.ChatMessageService;
import com.kood.backend.service.ConnectionService;
import lombok.RequiredArgsConstructor;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatMessageService chatMessageService;
    private final ConnectionService connectionService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    @MessageMapping("/messages")
    public void handleMessage(SendMessageDTO dto, Principal principal) {
        System.out.println("handleMessage called: from " + principal.getName() + " to " + dto.getReceiverId()
                + " content: " + dto.getContent());
        Long senderId = Long.parseLong(principal.getName());
        Long receiverId = dto.getReceiverId();

        Connection connection = connectionService
                .getExistingAcceptedConnection(senderId, receiverId);

        connection.setNumberOfMessages(connection.getNumberOfMessages() + 1);

        ChatMessage messageEntity = new ChatMessage();
        messageEntity.setSender(senderId);
        messageEntity.setReceiver(receiverId);
        messageEntity.setConnection(connection);
        messageEntity.setContent(dto.getContent());

        ChatMessage saved = chatMessageService.sendMessage(messageEntity);
        ChatMessageDTO response = ChatMessageMapper.toDTO(saved);

        messagingTemplate.convertAndSendToUser(Objects.requireNonNull(receiverId.toString()), "/queue/messages",
                Objects.requireNonNull(response));
        messagingTemplate.convertAndSendToUser(Objects.requireNonNull(senderId.toString()), "/queue/messages",
                Objects.requireNonNull(response));

    }

    @Component
    public class PresenceEventListener {

        @EventListener
        public void handleSessionConnected(SessionConnectedEvent event) {
            if (event == null)
                return;
            if (event.getUser() == null) {
                System.out
                        .println("WebSocket CONNECTED: User or username is null — connection rejected or unauthorized");
                return; // skip adding to onlineUsers
            } else if (Objects.requireNonNull(event.getUser()).getName() == null) {
                System.out
                        .println("WebSocket CONNECTED: User or username is null — connection rejected or unauthorized");
                return; // skip adding to onlineUsers
            }

            Principal principal = Objects.requireNonNull(event.getUser());
            System.out.println("SessionConnected: Principal name = " + principal.getName());
            if (principal == null || principal.getName() == null) {
                System.out.println("WebSocket CONNECTED: user/principal null, skipping");
                return;
            }
            String userId = principal.getName();
            String isOnline = "true";
            onlineUsers.put(userId, isOnline);
            System.out.println("WebSocket CONNECTED: userId=" + userId);
            broadcastOnlineUsers();
        }

        @EventListener
        public void handleSessionDisconnected(SessionDisconnectEvent event) {
            if (event.getUser() == null || Objects.requireNonNull(event.getUser()).getName() == null) {
                System.out
                        .println("WebSocket CONNECTED: User or username is null — connection rejected or unauthorized");
                return; // skip adding to onlineUsers
            }
            Principal principal = event.getUser();
            if (principal == null || principal.getName() == null) {
                System.out.println("WebSocket DISCONNECTED: user/principal null, skipping");
                return;
            }

            String userId = principal.getName();
            onlineUsers.remove(userId);
            System.out.println("WebSocket DISCONNECTED: userId=" + userId);
            broadcastOnlineUsers();
        }

        private void broadcastOnlineUsers() {
            System.out.println("Broadcasting online users to all clients...");
            messagingTemplate.convertAndSend("/topic/onlineUsers", Objects.requireNonNull(onlineUsers));
        }

    }

    @MessageMapping("/typing")
    public void typingIndicator(@Payload TypingStatus typingStatus, Principal principal) {
        String receiverId = Objects.requireNonNull(String.valueOf(typingStatus.getReceiverId()));
        if (onlineUsers.containsKey(receiverId)) {
            messagingTemplate.convertAndSendToUser(receiverId, "/queue/typing", typingStatus);
        } else {
            System.out.println("No online user found for userId " + receiverId);
        }
    }

    @MessageMapping("/onlineStatus")
    public void handleOnlineStatusRequest(@Payload OnlineStatusCheck friendIds, Principal principal) {
        System.out.println("Received online status request for friendIds: " + friendIds);

        Map<String, String> onlineStatusMap = new ConcurrentHashMap<>();

        if (friendIds.getFriendIds() == null || friendIds.getFriendIds().isEmpty()) {
            System.out.println("Error: No friend IDs to check!");
            return; // Handle empty list gracefully
        }
        onlineUsers.keySet().forEach(userId -> onlineStatusMap.put(userId, "true"));

        System.out.println("Filtered online status of connected friends: " + onlineStatusMap);

        String currentUserId = Objects.requireNonNull(principal.getName());
        messagingTemplate.convertAndSendToUser(currentUserId, "/queue/onlineStatusResponse", onlineStatusMap);

    }

    @MessageMapping("/connection-status-update-request")
    public void handleUserStatusRequest(@Payload UserStatusCheck check, Principal principal) {

        System.out.println("Received user status update request for me/friend: " + check.getMeId() + check.getUserId());
        if (check.getMeId() == check.getUserId()) {
            throw new DuplicateKeyException(
                    "Cannot send update check between two equal ids:" + check.getMeId() + " / " + check.getUserId());
        }
        if (check.getMeId() == null || check.getUserId() == null) {
            System.out.println("Error: No IDs to check!");
            return; // Handle empty list gracefully
        }
        Connection connection = connectionService.getConnection(check.getMeId(), check.getUserId());

        if (connection == null) {
            throw new NotFoundException(
                    "Connection not found between users: " + check.getMeId() + " and " + check.getUserId());
        }

        ConnectionUpdateNotification update = ConnectionMapper.toUpdateNotification(check);
        messagingTemplate.convertAndSendToUser(Objects.requireNonNull(check.getUserId().toString()),
                "/queue/connection-status-update",
                Objects.requireNonNull(update));

    }

}
