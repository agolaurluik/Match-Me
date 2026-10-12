package backend.chat.controller;

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

import backend.chat.dto.ChatMessageDTO;
import backend.chat.dto.SendMessageDTO;
import backend.chat.dto.TypingStatus;
import backend.chat.entity.ChatMessage;
import backend.chat.mapper.ChatMessageMapper;
import backend.chat.service.ChatMessageService;
import backend.connection.dto.ConnectionDetailedDTO;
import backend.connection.dto.ConnectionUpdateNotification;
import backend.connection.dto.OnlineStatusCheck;
import backend.connection.dto.UserStatusCheck;
import backend.connection.entity.Connection;
import backend.connection.mapper.ConnectionMapper;
import backend.connection.service.ConnectionService;
import backend.utilities.exceptions.NotFoundException;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatMessageService chatMessageService;
    private final ConnectionService connectionService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    @MessageMapping("/messages")
    public void handleMessage(SendMessageDTO dto, Principal principal) {

        Long senderId = Long.parseLong(principal.getName());
        Long receiverId = dto.getReceiverId();

        Connection connection = connectionService
                .getExistingAcceptedConnection(senderId, receiverId);

        connection.setNumberOfMessages(connection.getNumberOfMessages() + 1);

        ChatMessage messageEntity = new ChatMessage();
        messageEntity.setSenderId(senderId);
        messageEntity.setReceiverId(receiverId);
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
                return; // skip adding to onlineUsers
            } else if (Objects.requireNonNull(event.getUser()).getName() == null) {
                return; // skip adding to onlineUsers
            }

            Principal principal = Objects.requireNonNull(event.getUser());
            if (principal == null || principal.getName() == null) {
                return;
            }
            String userId = principal.getName();
            String isOnline = "true";
            onlineUsers.put(userId, isOnline);
            broadcastOnlineUsers();
        }

        @EventListener
        public void handleSessionDisconnected(SessionDisconnectEvent event) {
            if (event.getUser() == null || Objects.requireNonNull(event.getUser()).getName() == null) {
                return; // skip adding to onlineUsers
            }
            Principal principal = event.getUser();
            if (principal == null || principal.getName() == null) {
                return;
            }

            String userId = principal.getName();
            onlineUsers.remove(userId);
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
        }
    }

    @MessageMapping("/onlineStatus")
    public void handleOnlineStatusRequest(@Payload OnlineStatusCheck friendIds, Principal principal) {

        Map<String, String> onlineStatusMap = new ConcurrentHashMap<>();

        if (friendIds.getFriendIds() == null || friendIds.getFriendIds().isEmpty()) {
            return; // Handle empty list gracefully
        }
        onlineUsers.keySet().forEach(userId -> onlineStatusMap.put(userId, "true"));

        String currentUserId = Objects.requireNonNull(principal.getName());
        messagingTemplate.convertAndSendToUser(currentUserId, "/queue/onlineStatusResponse", onlineStatusMap);

    }

    @MessageMapping("/connection-status-update-request")
    public void handleUserStatusRequest(@Payload UserStatusCheck check, Principal principal) {

        if (check.getMeId() == check.getUserId()) {
            throw new DuplicateKeyException(
                    "Cannot send update check between two equal ids:" + check.getMeId() + " / " + check.getUserId());
        }
        if (check.getMeId() == null || check.getUserId() == null) {
            return; // Handle empty list gracefully
        }
        ConnectionDetailedDTO dto = connectionService.getConnection(check.getMeId(), check.getUserId());

        if (dto == null) {
            throw new NotFoundException(
                    "Connection not found between users: " + check.getMeId() + " and " + check.getUserId());
        }

        ConnectionUpdateNotification update = ConnectionMapper.toUpdateNotification(check);
        messagingTemplate.convertAndSendToUser(Objects.requireNonNull(check.getUserId().toString()),
                "/queue/connection-status-update",
                Objects.requireNonNull(update));

    }

}
