package com.kood.backend.repository;

import com.kood.backend.entity.ChatEntities.ChatMessage;
import com.kood.backend.entity.ChatEntities.MessageStatus;
import com.kood.backend.entity.ConnectionEntities.Connection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByConnectionOrderByTimestampDesc(Connection connection, Pageable pageable);

    List<ChatMessage> findByConnectionAndReceiverAndStatus(Connection connection, Long receiverId,
            MessageStatus status);

    Long countByReceiverAndStatus(Long receiverId, MessageStatus status);

    Optional<ChatMessage> findTopByConnectionIdOrderByTimestampDesc(Long connectionId);

    Long countByConnectionAndReceiverAndStatus(Connection connection, Long receiverId, MessageStatus status);

    Page<ChatMessage> findByConnection(Connection connection, Pageable pageable);

    void deleteAllByConnection(Connection connection);

}