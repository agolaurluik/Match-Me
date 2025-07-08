package com.kood.backend.controller;

import com.kood.backend.dto.ChatDTOs.ChatMessageDTO;
import com.kood.backend.entity.ChatEntities.ChatMessage;
import com.kood.backend.entity.ConnectionEntities.Connection;
import com.kood.backend.mapper.ChatMessageMapper;
import com.kood.backend.security.UserDetailsImpl;
import com.kood.backend.service.ChatMessageService;
import com.kood.backend.service.ConnectionService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

        private final ChatMessageService chatMessageService;
        private final ConnectionService connectionService;

        @GetMapping("/history")
        public Page<ChatMessageDTO> getChatHistory(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestParam Long connectionId,
                        @RequestParam int page,
                        @RequestParam int size) {

                Connection connection = connectionService.getConnectionById(connectionId);

                Page<ChatMessage> messagesPage = chatMessageService.getChatHistory(connection, page, size);

                return messagesPage.map(ChatMessageMapper::toDTO);
        }

        @PatchMapping("/read")
        public ResponseEntity<Map<String, String>> markMessagesAsRead(
                        @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam Long connectionId) {
                Connection connection = connectionService.getConnectionById(connectionId);
                chatMessageService.markMessagesAsRead(connection, userDetails.getId());

                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                return ResponseEntity.ok(response);
        }
}
