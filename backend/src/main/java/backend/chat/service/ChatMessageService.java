package backend.chat.service;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import backend.chat.dto.ChatMessageDTO;
import backend.chat.dto.SendMessageDTO;
import backend.chat.entity.ChatMessage;
import backend.chat.entity.MessageStatus;
import backend.chat.mapper.ChatMessageMapper;
import backend.chat.repository.ChatMessageRepository;
import backend.connection.entity.Connection;
import backend.connection.repository.ConnectionRepository;
import backend.connection.service.ConnectionService;
import backend.utilities.exceptions.BadRequestException;
import backend.utilities.exceptions.NotFoundException;

import java.util.List;

import java.util.Optional;

import java.time.Instant;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ConnectionService connectionService;
    private final ChatMessageRepository chatMessageRepository;
    private final ConnectionRepository connectionRepository;

    // Create
    public ChatMessage createChatMessage(ChatMessageDTO newChatMessage) {
        if (newChatMessage != null) {
            ChatMessage createdChatMessage = new ChatMessage();
            createdChatMessage.setSenderId(newChatMessage.getSenderId());
            createdChatMessage.setReceiverId(newChatMessage.getReceiverId());
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
        Long incomindChatMessageId = incomingChatMessage.getMessageId();
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

    public ChatMessage sendMessage(Long senderId, SendMessageDTO dto) {

        Long receiverId = dto.getReceiverId();

        Connection connection = connectionService
                .getExistingAcceptedConnection(senderId, receiverId);

        connection.setNumberOfMessages(connection.getNumberOfMessages() + 1);

        ChatMessage messageEntity = new ChatMessage();
        messageEntity.setSenderId(senderId);
        messageEntity.setReceiverId(receiverId);
        messageEntity.setConnection(connection);
        messageEntity.setContent(dto.getContent());
        messageEntity.setTimestamp(Instant.now());
        messageEntity.setStatus(MessageStatus.SENT);

        connection.setLastMessageTimestamp(messageEntity.getTimestamp());
        connectionRepository.save(connection);

        return chatMessageRepository.save(messageEntity);
    }

    public Page<ChatMessage> getChatHistory(Connection connection, Pageable pageable) {
        return chatMessageRepository.findByConnectionOrderByTimestampDesc(connection, pageable);
    }

    public List<ChatMessage> getUnreadMessages(Connection connection, Long receiverId) {
        return chatMessageRepository.findByConnectionAndReceiverIdAndStatus(
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
        return chatMessageRepository.countByReceiverIdAndStatus(userId, MessageStatus.SENT);
    }

    public String getLastMessagePreview(Long connectionId, Long currentUserId) {
        return chatMessageRepository.findTopByConnectionIdOrderByTimestampDesc(connectionId)
                .map(message -> {
                    boolean sentByCurrentUser = message.getSenderId().equals(currentUserId);
                    String prefix = sentByCurrentUser ? "You: " : "Them: ";
                    return prefix + message.getContent();
                })
                .orElse("No messages yet");
    }

    public Long countUnreadMessagesInConnection(Connection connection, Long receiverId) {
        return chatMessageRepository.countByConnectionAndReceiverIdAndStatus(
                connection,
                receiverId,
                MessageStatus.SENT);
    }
}