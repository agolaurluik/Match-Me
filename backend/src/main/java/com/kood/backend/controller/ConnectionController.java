package com.kood.backend.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.kood.backend.security.UserDetailsImpl;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kood.backend.dto.ConnectionDTOs.FriendConnectionDTO;
import com.kood.backend.entity.ConnectionEntities.Connection;
import com.kood.backend.entity.ConnectionEntities.ConnectionStatus;
import com.kood.backend.service.ConnectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    // CREATE:

    @PostMapping("/create/{receiverId}")
    public ResponseEntity<Connection> create(@AuthenticationPrincipal UserDetailsImpl sender,
            @PathVariable Long receiverId) {
        return ResponseEntity.ok(connectionService.createConnection(sender.getId(), receiverId));
    }

    // GET:
    @GetMapping("/all")
    public ResponseEntity<List<Long>> getAll(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(connectionService.getAllConnections(user.getId()));
    }

    // Get connection involving the logged-in user regardless of status
    @GetMapping("/{userId}")
    public ResponseEntity<FriendConnectionDTO> getFriendConnection(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        Connection connection = connectionService.getConnection(user.getId(), userId);
        FriendConnectionDTO connectionDTO = new FriendConnectionDTO(connection, user.getId());
        return ResponseEntity.ok(connectionDTO);
    }

    @GetMapping
    public ResponseEntity<List<Long>> getConnections(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(connectionService.getConnections(user.getId()));
    }

    // A Liszt to return Friends for Chat window
    // That is, those users who have a connection with the logged-in user, and the
    // connection has the status ACCEPTED
    @GetMapping("/friend-liszt")
    public ResponseEntity<List<FriendConnectionDTO>> getFriendConnections(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<Connection> acceptedConnections = connectionService.getConnectionsByStatus(user.getId(),
                ConnectionStatus.ACCEPTED, true);
        List<FriendConnectionDTO> dtoLiszt = acceptedConnections.stream()
                .map(connection -> new FriendConnectionDTO(connection, user.getId())).toList();
        return ResponseEntity.ok(dtoLiszt);
    }

    // Get all accepted connections for the logged-in user
    @GetMapping("/accepted")
    public ResponseEntity<List<Connection>> getAllAccepted(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity
                .ok(connectionService.getConnectionsByStatus(user.getId(), ConnectionStatus.ACCEPTED, true));
    }

    // Get all pending connections for the logged-in user --- only really useful for
    // counting total connections in waiting
    @GetMapping("/pending")
    public ResponseEntity<List<Connection>> getAllPending(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity
                .ok(connectionService.getConnectionsByStatus(user.getId(), ConnectionStatus.PENDING, false));
    }

    // Get all blocked connections for the logged-in user (where they block others)
    @GetMapping("/blocked")
    public ResponseEntity<List<Connection>> getAllBlocked(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity
                .ok(connectionService.getConnectionsByStatus(user.getId(), ConnectionStatus.BLOCKED, false));
    }

    // Get all rejected connections for the logged-in user
    @GetMapping("/rejected")
    public ResponseEntity<List<Connection>> getAllRejected(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity
                .ok(connectionService.getConnectionsByStatus(user.getId(), ConnectionStatus.REJECTED, false));
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<Connection>> getAllIncoming(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(connectionService.getAllIncomingConnections(user.getId()));
    }

    @GetMapping("/outgoing")
    public ResponseEntity<List<Connection>> getAllOutgoing(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(connectionService.getAllOutgoingConnections(user.getId()));
    }

    // Accept a friend request if the logged-in user is the receiver of the
    // connection
    @PostMapping("/{userId}/accept")
    public ResponseEntity<Connection> accept(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        return ResponseEntity.ok(connectionService.acceptConnection(user.getId(), userId));
    }

    // Block a user in a connection if the logged-in user is part of that connection
    @PostMapping("/{userId}/block")
    public ResponseEntity<Connection> block(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        return ResponseEntity.ok(connectionService.blockConnection(user.getId(), userId));
    }

    // Unblock the current user by setting their side to ACCEPTED, the other remains
    // whatever it was before
    @PostMapping("/{userId}/unblock")
    public ResponseEntity<Connection> unblock(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long userId) {
        return ResponseEntity.ok(connectionService.unblockConnection(user.getId(), userId));
    }

    // Reject a user in a connection if the logged-in user is part of that
    // connection
    @PostMapping("/{userId}/reject")
    public ResponseEntity<Connection> reject(@AuthenticationPrincipal UserDetailsImpl user,
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
