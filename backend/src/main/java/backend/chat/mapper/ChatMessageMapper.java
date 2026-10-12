package backend.chat.mapper;

import backend.chat.dto.ChatMessageDTO;
import backend.chat.entity.ChatMessage;

public class ChatMessageMapper {

    public static ChatMessageDTO toDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .messageId(message.getId())
                .connectionId(message.getConnection().getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
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
        chatMessage.setId(chatMessageDTO.getMessageId());
        chatMessage.setTimestamp(chatMessageDTO.getTimestamp());
        chatMessage.setSenderId(chatMessageDTO.getSenderId());
        chatMessage.setReceiverId(chatMessageDTO.getReceiverId());
        chatMessage.setContent(chatMessageDTO.getContent());
        return chatMessage;
    }
}