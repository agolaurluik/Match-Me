package backend.connection.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import backend.chat.repository.ChatMessageRepository;
import backend.connection.dto.ConnectionDetailedDTO;
import backend.connection.entity.Connection;
import backend.connection.entity.ConnectionStatus;
import backend.connection.mapper.ConnectionMapper;
import backend.connection.repository.ConnectionRepository;
import backend.matching.dto.UserMatchDetailDTO;
import backend.users.service.UserService;
import backend.utilities.HelperMethods.MatchSigningUtility;
import backend.utilities.exceptions.BadRequestException;
import backend.utilities.exceptions.NotFoundException;

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
    private final MatchSigningUtility matchSigningUtility;
    private final UserService userService;

    // CREATE

    @Override
    public ConnectionDetailedDTO createConnection(Long senderId, UserMatchDetailDTO dto) {

        matchSigningUtility.validateMatchData(senderId, dto);
        Long receiverId = dto.getReceiverId();

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

        connection.setMatchPercentage(dto.getMatchPercentage());
        connection.setDistance(dto.getDistance());
        connection.setSharedInterestsSet(
                new HashSet<>(dto.getSharedInterestsSet()));
        connection.setSharedPersonalitiesSet(
                new HashSet<>(dto.getSharedPersonalitiesSet()));

        Connection saved = connectionRepository.save(connection);

        ConnectionDetailedDTO savedDto = ConnectionMapper.toDetailedDTO(saved);
        return savedDto;
    }

    // GET
    @Override
    public ConnectionDetailedDTO getConnection(Long meId, Long userId) {
        Connection connection = connectionRepository.findBySenderAndReceiver(meId, userId)
                .or(() -> connectionRepository.findBySenderAndReceiver(userId, meId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Connection does not exist between these users, unable to proceed."));

        ConnectionDetailedDTO dto = ConnectionMapper.toDetailedDTO(connection);
        return dto;
    }

    @Override
    public ArrayList<Long> getAllConnections(Long userId) {
        List<Connection> sent = connectionRepository.findAllBySender(userId);
        List<Connection> received = connectionRepository.findAllByReceiver(userId);

        List<Connection> all = new ArrayList<>();
        all.addAll(sent);
        all.addAll(received);

        all.sort(Comparator.comparing(connection -> connection.getCreatedAt()));

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
    public List<ConnectionDetailedDTO> getConnections(Long userId) {
        List<ConnectionDetailedDTO> acceptedConnections = getConnectionsByStatus(userId,
                ConnectionStatus.ACCEPTED, true);
        List<ConnectionDetailedDTO> blockedConnections = getConnectionsByStatus(userId,
                ConnectionStatus.BLOCKED, true);
        List<ConnectionDetailedDTO> connections = new ArrayList<>();
        connections.addAll(acceptedConnections);
        connections.addAll(blockedConnections);

        return connections;
    }

    @Override
    public List<ConnectionDetailedDTO> getConnectionsByStatus(Long userId, ConnectionStatus status,
            boolean acceptedView) {
        List<Connection> result = new ArrayList<>();
        if (acceptedView) {
            result.addAll(connectionRepository.findByReceiverAndReceiverStatus(userId, status));
            result.addAll(connectionRepository.findBySenderAndSenderStatus(userId, status));
        } else {
            result.addAll(connectionRepository.findByReceiverAndReceiverStatus(userId, status));
        }

        result.removeIf(conn -> conn.getSender().equals(userId) && conn.getReceiver().equals(userId));

        List<ConnectionDetailedDTO> filteredConnectionDTOs = new ArrayList<>();
        for (Connection existingConnection : result) {
            ConnectionDetailedDTO dto = ConnectionMapper.toDetailedDTO(existingConnection);
            filteredConnectionDTOs.add(dto);
        }
        return filteredConnectionDTOs;
    }

    public Set<UserMatchDetailDTO> getAllIncomingConnections(Long userId) {

        List<Connection> A1 = connectionRepository.findByReceiverAndReceiverStatus(userId, ConnectionStatus.PENDING);
        Set<UserMatchDetailDTO> incomingConnections = new HashSet<>();

        for (Connection existingConnection : A1) {
            UserMatchDetailDTO dto = ConnectionMapper.toUserMatchDetailDTO(existingConnection);

            String senderImageName = userService.getUserProfileImageNameByUserId(dto.getSenderId());
            String receiverProfileImageName = userService.getUserProfileImageNameByUserId(dto.getReceiverId());
            dto.setSenderProfileImageName(senderImageName);
            dto.setReceiverProfileImageName(receiverProfileImageName);

            incomingConnections.add(dto);
        }
        return incomingConnections;
    }

    public Set<UserMatchDetailDTO> getAllOutgoingConnections(Long userId) {
        List<Connection> O1 = connectionRepository.findBySenderAndSenderStatus(userId, ConnectionStatus.PENDING);
        Set<UserMatchDetailDTO> outgoingConnections = new HashSet<>();

        for (Connection existingConnection : O1) {
            UserMatchDetailDTO dto = ConnectionMapper.toUserMatchDetailDTO(existingConnection);

            String senderImageName = userService.getUserProfileImageNameByUserId(dto.getSenderId());
            String receiverProfileImageName = userService.getUserProfileImageNameByUserId(dto.getReceiverId());
            dto.setSenderProfileImageName(senderImageName);
            dto.setReceiverProfileImageName(receiverProfileImageName);

            outgoingConnections.add(dto);
        }

        return outgoingConnections;
    }

    @Override
    public ConnectionDetailedDTO acceptConnection(Long receiverId, Long senderId) {
        Connection connection = connectionRepository.findBySenderAndReceiver(senderId, receiverId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No connection found where the user is receiver and pending"));

        if (!connection.getReceiver().equals(receiverId)) {
            throw new SecurityException("Only the receiver can accept this request");
        }

        connection.setSenderStatus(ConnectionStatus.ACCEPTED);
        connection.setReceiverStatus(ConnectionStatus.ACCEPTED);
        connectionRepository.save(connection);

        ConnectionDetailedDTO dto = ConnectionMapper.toDetailedDTO(connection);
        return dto;
    }

    public ConnectionDetailedDTO rejectConnection(Long meId, Long userId) {

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
        connectionRepository.save(connection);

        ConnectionDetailedDTO dto = ConnectionMapper.toDetailedDTO(connection);
        return dto;
    }

    @Override
    public ConnectionDetailedDTO blockConnection(Long meId, Long userId) {
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

        connectionRepository.save(connection);
        ConnectionDetailedDTO dto = ConnectionMapper.toDetailedDTO(connection);
        return dto;
    }

    @Override
    public ConnectionDetailedDTO unblockConnection(Long meId, Long userId) {
        Connection connection = connectionRepository.findBySenderAndReceiver(meId, userId)
                .or(() -> connectionRepository.findBySenderAndReceiver(userId, meId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Connection does not exist between these users, unable to proceed."));

        if (connection.getSender().equals(meId)) {
            connection.setSenderStatus(ConnectionStatus.ACCEPTED);
        } else if (connection.getReceiver().equals(meId)) {
            connection.setReceiverStatus(ConnectionStatus.ACCEPTED);
        }
        connectionRepository.save(connection);
        ConnectionDetailedDTO dto = ConnectionMapper.toDetailedDTO(connection);
        return dto;
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

        if (existingConnection == null) {
            throw new NotFoundException("Connection not found within database");
        }
        if (!isUserInConnection(receiverId, existingConnection)) {
            throw new SecurityException("User not authorized to delete this connection");
        }

        chatMessageRepository.deleteAllByConnection(existingConnection);
        connectionRepository.delete(existingConnection);
    }

    // For chat messages mainly
    @Override
    public Connection getExistingAcceptedConnection(Long senderId, Long receiverId) {
        Connection foundConnection = connectionRepository.findByAcceptedParticipants(senderId, receiverId)
                .orElseThrow(() -> new EntityNotFoundException("No accepted connection found between users"));
        return foundConnection;
    }

    @Override
    public Connection getConnectionByConnectionId(Long connectionId) {
        if (connectionId == null)
            throw new BadRequestException("ConnectionId provided is null");
        return connectionRepository.findById(connectionId)
                .orElseThrow(() -> new EntityNotFoundException("No connection with the id: " + connectionId));
    }

    private boolean isUserInConnection(Long userId, Connection connection) {
        return connection.getReceiver().equals(userId) ||
                connection.getSender().equals(userId);
    }

    @Override
    public boolean canView(Long senderId, Long receiverId) {

        if (senderId == null || receiverId == null) {
            return false;
        }

        if (senderId.equals(receiverId)) {
            return true;
        }

        List<ConnectionDetailedDTO> acceptedConnections = getConnectionsByStatus(senderId, ConnectionStatus.ACCEPTED,
                true);
        List<ConnectionDetailedDTO> pendingConnections = getConnectionsByStatus(senderId, ConnectionStatus.PENDING,
                true);

        Set<Long> validConnectionIds = new HashSet<>();

        for (ConnectionDetailedDTO item : acceptedConnections) {
            validConnectionIds.add(item.getSenderId());
            validConnectionIds.add(item.getReceiverId());
        }

        for (ConnectionDetailedDTO item : pendingConnections) {
            validConnectionIds.add(item.getSenderId());
            validConnectionIds.add(item.getReceiverId());
        }

        return validConnectionIds.contains(receiverId);
    }
}