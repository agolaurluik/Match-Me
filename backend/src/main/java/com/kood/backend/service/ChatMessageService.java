package com.kood.backend.service;

import com.kood.backend.dto.ChatDTOs.ChatMessageDTO;
import com.kood.backend.entity.ChatEntities.ChatMessage;
import com.kood.backend.entity.ChatEntities.MessageStatus;
import com.kood.backend.entity.ConnectionEntities.Connection;
import com.kood.backend.repository.ChatMessageRepository;
import com.kood.backend.repository.ConnectionRepository;
import com.kood.backend.mapper.ChatMessageMapper;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import java.util.Optional;

import java.time.Instant;

import com.kood.backend.exceptions.BadRequestException;
import com.kood.backend.exceptions.NotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConnectionRepository connectionRepository;

    // Create
    public ChatMessage createChatMessage(ChatMessageDTO newChatMessage) {
        System.err.println("Creating chat message");
        if (newChatMessage != null) {
            System.err.println("incoming Message is not null, creating entity");
            ChatMessage createdChatMessage = new ChatMessage();
            createdChatMessage.setSender(newChatMessage.getSender());
            createdChatMessage.setReceiver(newChatMessage.getReceiver());
            createdChatMessage.setContent(newChatMessage.getContent());
            createdChatMessage.setTimestamp(newChatMessage.getTimestamp());
            chatMessageRepository.save(createdChatMessage);
            return createdChatMessage;
        } else {
            throw new NotFoundException("ChatMessage is null or missing, please add a valid <String> in your request");
        }
    }

    // Retrieve
    public ChatMessageDTO getChatMessageById(@NonNull Long id) {
        Optional<ChatMessage> existingChatMessage = chatMessageRepository.findById(id);
        if (existingChatMessage.isPresent()) {
            ChatMessageDTO foundChatMessageDTO = ChatMessageMapper.toDTO(existingChatMessage.get());
            return foundChatMessageDTO;
        }
        throw new BadRequestException("ChatMessage with the id: " + id + "not found.");
    }

    public Page<ChatMessage> getChatHistory(Connection connection, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return chatMessageRepository.findByConnection(connection, pageable);
    }

    // Update

    @Transactional
    public ChatMessageDTO updateChatMessage(ChatMessageDTO incomingChatMessage) {
        Long incomindChatMessageId = incomingChatMessage.getId();
        if (incomindChatMessageId != null) {
            Optional<ChatMessage> existingChatMessage = chatMessageRepository.findById(incomindChatMessageId);
            if (existingChatMessage.isPresent()) {
                ChatMessage updatedChatMessage = existingChatMessage.get();
                updatedChatMessage
                        .setTimestamp(incomingChatMessage.getTimestamp());
                chatMessageRepository.save(updatedChatMessage);
                return ChatMessageMapper.toDTO(updatedChatMessage);
            } else {
                createChatMessage(incomingChatMessage);
                return incomingChatMessage;
            }
        } else {
            createChatMessage(incomingChatMessage);
            return incomingChatMessage;
        }
    }

    // Delete
    public void deleteChatMessageById(@NonNull Long id) {
        if (chatMessageRepository.existsById(id)) {
            chatMessageRepository.deleteById(id);
        } else {
            throw new NotFoundException("No ChatMessage found with the id: " + id);
        }
    }

    public ChatMessage sendMessage(ChatMessage message) {
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.SENT);

        Connection connection = message.getConnection();
        connection.setLastMessageTimestamp(message.getTimestamp());
        connectionRepository.save(connection);

        return chatMessageRepository.save(message);
    }

    public Page<ChatMessage> getChatHistory(Connection connection, Pageable pageable) {
        return chatMessageRepository.findByConnectionOrderByTimestampDesc(connection, pageable);
    }

    public List<ChatMessage> getUnreadMessages(Connection connection, Long receiverId) {
        return chatMessageRepository.findByConnectionAndReceiverAndStatus(
                connection, receiverId, MessageStatus.SENT);
    }

    public void markMessagesAsRead(Connection connection, Long receiverId) {
        List<ChatMessage> unreadMessages = getUnreadMessages(connection, receiverId);
        unreadMessages.forEach(msg -> {
            msg.setStatus(MessageStatus.READ);
        });

        chatMessageRepository.saveAll(unreadMessages);
    }

    public Long countUnreadMessages(Long userId) {
        return chatMessageRepository.countByReceiverAndStatus(userId, MessageStatus.SENT);
    }

    public String getLastMessagePreview(Long connectionId, Long currentUserId) {
        return chatMessageRepository.findTopByConnectionIdOrderByTimestampDesc(connectionId)
                .map(message -> {
                    boolean sentByCurrentUser = message.getSender().equals(currentUserId);
                    String prefix = sentByCurrentUser ? "You: " : "Them: ";
                    return prefix + message.getContent();
                })
                .orElse("No messages yet");
    }

    public Long countUnreadMessagesInConnection(Connection connection, Long receiverId) {
        return chatMessageRepository.countByConnectionAndReceiverAndStatus(
                connection,
                receiverId,
                MessageStatus.SENT);
    }
}