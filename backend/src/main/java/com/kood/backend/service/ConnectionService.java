package com.kood.backend.service;

import org.springframework.stereotype.Service;

import com.kood.backend.entity.ConnectionEntities.Connection;
import com.kood.backend.entity.ConnectionEntities.ConnectionStatus;

import java.util.List;

@Service
public interface ConnectionService {

    Connection createConnection(Long senderId, Long receiverId);

    List<Long> getAllConnections(Long userId);

    List<Long> getConnections(Long userId);

    Connection getConnection(Long meId, Long userId);

    List<Connection> getConnectionsByStatus(Long userId, ConnectionStatus status, boolean acceptedView);

    List<Connection> getAllIncomingConnections(Long userId);

    List<Connection> getAllOutgoingConnections(Long userId);

    Connection acceptConnection(Long userId, Long candidateId);

    Connection blockConnection(Long userId, Long candidateId);

    Connection unblockConnection(Long userId, Long candidateId);

    Connection rejectConnection(Long userId, Long candidateId);

    void deleteConnectionIfNoMessages(Long userId, Long candidateId);

    void forceDeleteConnection(Long userId, Long candidateId);

    public Connection getExistingAcceptedConnection(Long senderId, Long receiverId);

    Connection getConnectionById(Long connectionId);
}
