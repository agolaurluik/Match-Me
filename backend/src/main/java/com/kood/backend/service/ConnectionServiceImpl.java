package com.kood.backend.service;

import com.kood.backend.entity.ConnectionEntities.Connection;
import com.kood.backend.entity.ConnectionEntities.ConnectionStatus;
import com.kood.backend.repository.ChatMessageRepository;
import com.kood.backend.repository.ConnectionRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final ChatMessageRepository chatMessageRepository;

    // CREATE

    @Override
    public Connection createConnection(Long senderId, Long receiverId) {

        if (receiverId.equals(senderId)) {
            throw new IllegalArgumentException("You cannot create a connection with yourself.");
        }
        if (connectionRepository.findBySenderAndReceiver(senderId, receiverId).isPresent()) {
            throw new IllegalStateException("Friend request already sent");
        }
        if (connectionRepository.findBySenderAndReceiver(receiverId, senderId).isPresent()) {
            throw new IllegalStateException("This user has already sent you a request");
        }
        Connection connection = new Connection();

        connection.setSender(senderId);
        connection.setReceiver(receiverId);
        connection.setCreatedAt(Instant.now());
        connection.setLastMessageTimestamp(null);
        connection.setNumberOfMessages(0);
        connection.setSenderStatus(ConnectionStatus.PENDING);
        connection.setReceiverStatus(ConnectionStatus.PENDING);
        Connection saved = connectionRepository.save(connection);

        return saved;
    }

    // GET
    @Override
    public Connection getConnection(Long meId, Long userId) {
        Connection connection = connectionRepository.findBySenderAndReceiver(meId, userId)
                .or(() -> connectionRepository.findBySenderAndReceiver(userId, meId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Connection does not exist between these users, unable to proceed."));

        return connection;
    }

    @Override
    public ArrayList<Long> getAllConnections(Long userId) {
        List<Connection> sent = connectionRepository.findAllBySender(userId);
        List<Connection> received = connectionRepository.findAllByReceiver(userId);

        List<Connection> all = new ArrayList<>();
        all.addAll(sent);
        all.addAll(received);

        all.sort(Comparator.comparing(Connection::getCreatedAt));

        List<Long> otherUserIds = new ArrayList<>();

        for (Connection connection : all) {
            if (connection.getSender() != userId) {
                otherUserIds.add(connection.getSender());
            }
            if (connection.getReceiver() != userId) {
                otherUserIds.add(connection.getReceiver());
            }
        }

        Set<Long> uniqueUserIds = new HashSet<>(otherUserIds);
        return new ArrayList<>(uniqueUserIds);
    }

    @Override
    public ArrayList<Long> getConnections(Long userId) {
        List<Connection> acceptedConnections = getConnectionsByStatus(userId,
                ConnectionStatus.ACCEPTED, true);
        List<Connection> blockedConnections = getConnectionsByStatus(userId,
                ConnectionStatus.BLOCKED, true);
        List<Connection> connections = new ArrayList<>();
        connections.addAll(acceptedConnections);
        connections.addAll(blockedConnections);

        ArrayList<Long> otherUserIdsList = new ArrayList<>();

        for (Connection conn : connections) {
            if (conn.getReceiver().equals(userId)) {
                otherUserIdsList.add(conn.getSender());
            } else if (conn.getSender().equals(userId)) {
                otherUserIdsList.add(conn.getReceiver());
            }
        }
        return otherUserIdsList;
    }

    @Override
    public List<Connection> getConnectionsByStatus(Long userId, ConnectionStatus status, boolean acceptedView) {
        List<Connection> result = new ArrayList<>();
        if (acceptedView) {
            result.addAll(connectionRepository.findByReceiverAndReceiverStatus(userId, status));
            result.addAll(connectionRepository.findBySenderAndSenderStatus(userId, status));
        } else {
            result.addAll(connectionRepository.findByReceiverAndReceiverStatus(userId, status));
        }

        result.removeIf(conn -> conn.getSender().equals(userId) && conn.getReceiver().equals(userId));

        return result;
    }

    public List<Connection> getAllIncomingConnections(Long userId) {
        System.err.println("Fetching incoming connections for userId: " + userId);
        List<Connection> A1 = connectionRepository.findByReceiverAndReceiverStatus(userId, ConnectionStatus.PENDING);
        return A1;
    }

    public List<Connection> getAllOutgoingConnections(Long userId) {
        System.err.println("Fetching outgoing connections for userId: " + userId);
        List<Connection> A1 = connectionRepository.findBySenderAndSenderStatus(userId, ConnectionStatus.PENDING);
        return A1;
    }

    @Override
    public Connection acceptConnection(Long receiverId, Long senderId) {
        Connection connection = connectionRepository.findBySenderAndReceiver(senderId, receiverId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No connection found where the user is receiver and pending"));

        if (!connection.getReceiver().equals(receiverId)) {
            throw new SecurityException("Only the receiver can accept this request");
        }

        connection.setSenderStatus(ConnectionStatus.ACCEPTED);
        connection.setReceiverStatus(ConnectionStatus.ACCEPTED);
        return connectionRepository.save(connection);
    }

    public Connection rejectConnection(Long meId, Long userId) {

        Connection connection = connectionRepository.findBySenderAndReceiver(meId, userId)
                .or(() -> connectionRepository.findBySenderAndReceiver(userId, meId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Connection does not exist between these users, unable to proceed."));

        if (connection.getSender().equals(meId)) {
            connection.setSenderStatus(ConnectionStatus.REJECTED);
            if (connection.getReceiverStatus() == ConnectionStatus.ACCEPTED || connection
                    .getReceiverStatus() == ConnectionStatus.BLOCKED) {
                connection.setReceiverStatus(ConnectionStatus.BLOCKED);
            } else {
                connection.setReceiverStatus(ConnectionStatus.REJECTED);
            }

        } else if (connection.getReceiver().equals(meId)) {
            connection.setReceiverStatus(ConnectionStatus.REJECTED);
            if (connection.getSenderStatus() == ConnectionStatus.ACCEPTED
                    || connection.getSenderStatus() == ConnectionStatus.BLOCKED) {
                connection.setSenderStatus(ConnectionStatus.BLOCKED);
            } else {
                connection.setSenderStatus(ConnectionStatus.REJECTED);
            }
        }
        return connectionRepository.save(connection);
    }

    @Override
    public Connection blockConnection(Long meId, Long userId) {
        Connection connection = connectionRepository.findBySenderAndReceiver(meId, userId)
                .or(() -> connectionRepository.findBySenderAndReceiver(userId, meId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Connection does not exist between these users, unable to proceed."));

        if (connection.getSender().equals(meId)) {
            connection.setSenderStatus(ConnectionStatus.BLOCKED);
            if (connection.getReceiverStatus() != ConnectionStatus.ACCEPTED) {
                connection.setReceiverStatus(ConnectionStatus.REJECTED);
            }
        } else if (connection.getReceiver().equals(meId)) {
            connection.setReceiverStatus(ConnectionStatus.BLOCKED);
            if (connection.getSenderStatus() != ConnectionStatus.ACCEPTED) {
                connection.setSenderStatus(ConnectionStatus.REJECTED);
            }
        }

        return connectionRepository.save(connection);
    }

    @Override
    public Connection unblockConnection(Long meId, Long userId) {
        Connection connection = connectionRepository.findBySenderAndReceiver(meId, userId)
                .or(() -> connectionRepository.findBySenderAndReceiver(userId, meId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Connection does not exist between these users, unable to proceed."));

        if (connection.getSender().equals(meId)) {
            connection.setSenderStatus(ConnectionStatus.ACCEPTED);
        } else if (connection.getReceiver().equals(meId)) {
            connection.setReceiverStatus(ConnectionStatus.ACCEPTED);
        }
        return connectionRepository.save(connection);
    }

    @Override
    public void deleteConnectionIfNoMessages(Long meId, Long userId) {
        Connection connection = connectionRepository.findBySenderAndReceiver(meId, userId)
                .or(() -> connectionRepository.findBySenderAndReceiver(userId, meId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Connection does not exist between these users, unable to proceed."));

        if (!isUserInConnection(userId, connection)) {
            throw new SecurityException("User not authorized to delete this connection");
        }

        boolean allowed = (connection.getSenderStatus() == ConnectionStatus.REJECTED
                && connection.getReceiverStatus() == ConnectionStatus.REJECTED) ||
                (connection.getSenderStatus() == ConnectionStatus.BLOCKED
                        && connection.getReceiverStatus() == ConnectionStatus.BLOCKED)
                ||
                (connection.getSenderStatus() == ConnectionStatus.REJECTED
                        && connection.getReceiverStatus() == ConnectionStatus.BLOCKED)
                ||
                (connection.getSenderStatus() == ConnectionStatus.BLOCKED
                        && connection.getReceiverStatus() == ConnectionStatus.REJECTED)
                ||
                (connection.getSenderStatus() == ConnectionStatus.PENDING
                        && connection.getReceiverStatus() == ConnectionStatus.PENDING);

        if (!allowed) {
            throw new IllegalStateException(
                    "Cannot delete unless both parties have rejected, blocked, or are still pending.");
        }

        int messageCount = connection.getNumberOfMessages();
        if (messageCount == 0) {
            connectionRepository.delete(connection);
        } else {
            throw new IllegalStateException("Connection has messages and cannot be deleted");
        }
    }

    @Override
    public void forceDeleteConnection(Long receiverId, Long senderId) {
        Connection existingConnection = connectionRepository.findBySenderAndReceiver(senderId, receiverId)
                .or(() -> connectionRepository.findBySenderAndReceiver(receiverId, senderId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Connection does not exist between these users, nothing to delete"));

        if (!isUserInConnection(receiverId, existingConnection)) {
            throw new SecurityException("User not authorized to delete this connection");
        }

        chatMessageRepository.deleteAllByConnection(existingConnection);
        connectionRepository.delete(existingConnection);
    }

    @Override
    public Connection getExistingAcceptedConnection(Long senderId, Long receiverId) {
        return connectionRepository.findByAcceptedParticipants(senderId, receiverId)
                .orElseThrow(() -> new EntityNotFoundException("No accepted connection found between users"));
    }

    @Override
    public Connection getConnectionById(Long connectionId) {
        return connectionRepository.findById(connectionId)
                .orElseThrow(() -> new EntityNotFoundException("No connection with the id: " + connectionId));
    }

    private boolean isUserInConnection(Long userId, Connection connection) {
        return connection.getReceiver().equals(userId) ||
                connection.getSender().equals(userId);
    }
}