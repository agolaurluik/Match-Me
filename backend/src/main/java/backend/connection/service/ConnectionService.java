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

    Connection acceptConnection(Long userId, Long candidateId);

    Connection blockConnection(Long userId, Long candidateId);

    Connection unblockConnection(Long userId, Long candidateId);

    Connection rejectConnection(Long userId, Long candidateId);

    void deleteConnectionIfNoMessages(Long userId, Long candidateId);

    void forceDeleteConnection(Long userId, Long candidateId);

    public Connection getExistingAcceptedConnection(Long senderId, Long receiverId);

    Connection getConnectionById(Long connectionId);

    boolean canView(Long requesterId, Long viewingId);
}
