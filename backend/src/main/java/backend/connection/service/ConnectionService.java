package backend.connection.service;

import org.springframework.stereotype.Service;

import backend.connection.dto.ConnectionDetailedDTO;
import backend.connection.entity.Connection;
import backend.connection.entity.ConnectionStatus;
import backend.matching.dto.UserMatchDetailDTO;

import java.util.List;
import java.util.Set;

@Service
public interface ConnectionService {

    ConnectionDetailedDTO createConnection(Long senderId, UserMatchDetailDTO dto);

    List<Long> getAllConnections(Long userId);

    List<ConnectionDetailedDTO> getConnections(Long userId);

    ConnectionDetailedDTO getConnection(Long meId, Long userId);

    List<ConnectionDetailedDTO> getConnectionsByStatus(Long userId, ConnectionStatus status, boolean acceptedView);

    Set<UserMatchDetailDTO> getAllIncomingConnections(Long userId);

    Set<UserMatchDetailDTO> getAllOutgoingConnections(Long userId);

    ConnectionDetailedDTO acceptConnection(Long userId, Long receiverId);

    ConnectionDetailedDTO blockConnection(Long userId, Long receiverId);

    ConnectionDetailedDTO unblockConnection(Long userId, Long receiverId);

    ConnectionDetailedDTO rejectConnection(Long userId, Long receiverId);

    void deleteConnectionIfNoMessages(Long userId, Long receiverId);

    void forceDeleteConnection(Long userId, Long receiverId);

    public Connection getExistingAcceptedConnection(Long senderId, Long receiverId);

    Connection getConnectionByConnectionId(Long connectionId);

    boolean confirmSeenConnectionStatusUpdates(List<Long> dto);

    boolean userHasUnseenConnections(Long userId);

    boolean canView(Long requesterId, Long viewingId);
}
