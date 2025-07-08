package com.kood.backend.mapper;

import com.kood.backend.dto.ChatDTOs.ChatMessageDTO;
import com.kood.backend.entity.ChatEntities.ChatMessage;

public class ChatMessageMapper {

    public static ChatMessageDTO toDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .connectionId(message.getConnection().getId())
                .sender(message.getSender())
                .receiver(message.getReceiver())
                .content(message.getContent())
                .status(message.getStatus().name())
                .timestamp(message.getTimestamp())
                .build();
    }

    public static ChatMessage toEntity(ChatMessageDTO chatMessageDTO) {
        if (chatMessageDTO == null) {
            return null;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(chatMessageDTO.getId());
        chatMessage.setTimestamp(chatMessageDTO.getTimestamp());
        chatMessage.setSender(chatMessageDTO.getSender());
        chatMessage.setReceiver(chatMessage.getReceiver());
        chatMessage.setContent(chatMessageDTO.getContent());
        return chatMessage;
    }
}