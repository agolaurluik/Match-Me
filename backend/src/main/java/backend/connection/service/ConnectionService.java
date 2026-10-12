package backend.connection.service;

import org.springframework.stereotype.Service;

import backend.connection.entity.Connection;
import backend.connection.entity.ConnectionStatus;
import backend.matching.dto.UserMatchDetailDTO;

import java.util.List;
import java.util.Set;

@Service
public interface ConnectionService {

    Connection createConnection(Long senderId, UserMatchDetailDTO dto);

    List<Long> getAllConnections(Long userId);

    List<Long> getConnections(Long userId);

    Connection getConnection(Long meId, Long userId);

    List<Connection> getConnectionsByStatus(Long userId, ConnectionStatus status, boolean acceptedView);

    Set<UserMatchDetailDTO> getAllIncomingConnections(Long userId);

    Set<UserMatchDetailDTO> getAllOutgoingConnections(Long userId);

    Connection acceptConnection(Long userId, Long receiverId);

    Connection blockConnection(Long userId, Long receiverId);

    Connection unblockConnection(Long userId, Long receiverId);

    Connection rejectConnection(Long userId, Long receiverId);

    void deleteConnectionIfNoMessages(Long userId, Long receiverId);

    void forceDeleteConnection(Long userId, Long receiverId);

    public Connection getExistingAcceptedConnection(Long senderId, Long receiverId);

    Connection getConnectionById(Long connectionId);

    boolean canView(Long requesterId, Long viewingId);
}
