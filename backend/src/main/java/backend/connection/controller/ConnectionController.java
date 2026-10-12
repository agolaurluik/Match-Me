package backend.connection.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.connection.dto.ConnectionDetailedDTO;
import backend.connection.entity.ConnectionStatus;
import backend.connection.service.ConnectionService;
import backend.matching.dto.UserMatchDetailDTO;
import backend.utilities.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    // CREATE:

    @PostMapping("/create")
    public ResponseEntity<ConnectionDetailedDTO> create(@AuthenticationPrincipal UserDetailsImpl sender,
            @RequestBody UserMatchDetailDTO dto) {
        return ResponseEntity.ok(connectionService.createConnection(sender.getId(), dto));
    }

    // Get connection involving the logged-in user regardless of status
    @GetMapping("/{userId}")
    public ResponseEntity<ConnectionDetailedDTO> getFriendConnection(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        ConnectionDetailedDTO dto = connectionService.getConnection(user.getId(), userId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ConnectionDetailedDTO>> getConnections(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(connectionService.getConnections(user.getId()));
    }

    // Get all accepted connections for the logged-in user
    @GetMapping("/accepted")
    public ResponseEntity<List<ConnectionDetailedDTO>> getAllAccepted(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity
                .ok(connectionService.getConnectionsByStatus(user.getId(), ConnectionStatus.ACCEPTED, true));
    }

    // Get all pending connections for the logged-in user --- only really useful for
    // counting total connections in waiting
    @GetMapping("/pending")
    public ResponseEntity<List<ConnectionDetailedDTO>> getAllPending(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity
                .ok(connectionService.getConnectionsByStatus(user.getId(), ConnectionStatus.PENDING, false));
    }

    // Get all blocked connections for the logged-in user (where they block others)
    @GetMapping("/blocked")
    public ResponseEntity<List<ConnectionDetailedDTO>> getAllBlocked(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity
                .ok(connectionService.getConnectionsByStatus(user.getId(), ConnectionStatus.BLOCKED, false));
    }

    // Get all rejected connections for the logged-in user
    @GetMapping("/rejected")
    public ResponseEntity<List<ConnectionDetailedDTO>> getAllRejected(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity
                .ok(connectionService.getConnectionsByStatus(user.getId(), ConnectionStatus.REJECTED, false));
    }

    @GetMapping("/incoming")
    public ResponseEntity<Set<UserMatchDetailDTO>> getAllIncoming(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(connectionService.getAllIncomingConnections(user.getId()));
    }

    @GetMapping("/outgoing")
    public ResponseEntity<Set<UserMatchDetailDTO>> getAllOutgoing(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(connectionService.getAllOutgoingConnections(user.getId()));
    }

    // Accept a friend request if the logged-in user is the receiver of the
    // connection
    @PostMapping("/{userId}/accept")
    public ResponseEntity<ConnectionDetailedDTO> accept(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        return ResponseEntity.ok(connectionService.acceptConnection(user.getId(), userId));
    }

    // Block a user in a connection if the logged-in user is part of that connection
    @PostMapping("/{userId}/block")
    public ResponseEntity<ConnectionDetailedDTO> block(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        return ResponseEntity.ok(connectionService.blockConnection(user.getId(), userId));
    }

    // Unblock the current user by setting their side to ACCEPTED, the other remains
    // whatever it was before
    @PostMapping("/{userId}/unblock")
    public ResponseEntity<ConnectionDetailedDTO> unblock(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        return ResponseEntity.ok(connectionService.unblockConnection(user.getId(), userId));
    }

    // Reject a user in a connection if the logged-in user is part of that
    // connection
    @PostMapping("/{userId}/reject")
    public ResponseEntity<ConnectionDetailedDTO> reject(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        return ResponseEntity.ok(connectionService.rejectConnection(user.getId(), userId));
    }

    // Delete a connection only if there are no associated messages
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<?> deleteIfNoMessages(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        connectionService.deleteConnectionIfNoMessages(user.getId(), userId);
        return ResponseEntity.noContent().build();
    }

    // Force delete a connection along with its messages
    @DeleteMapping("/{userId}/force")
    public ResponseEntity<?> forceDelete(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        connectionService.forceDeleteConnection(user.getId(), userId);
        return ResponseEntity.noContent().build();
    }

}
