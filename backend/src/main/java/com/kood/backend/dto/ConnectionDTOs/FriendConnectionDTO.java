package com.kood.backend.dto.ConnectionDTOs;

import java.time.Instant;

import com.kood.backend.entity.ConnectionEntities.Connection;
import com.kood.backend.entity.ConnectionEntities.ConnectionStatus;

import lombok.Data;

@Data
public class FriendConnectionDTO {
    private Long connectionId;
    private Long myId;
    private Long friendId;
    private boolean userIsReceiver;
    private Instant createdAt;
    private Instant lastMessageTimestamp;
    private int numberOfMessages;
    private ConnectionStatus senderStatus;
    private ConnectionStatus receiverStatus;

    public FriendConnectionDTO(Connection connection, Long loggedInUserId) {
        this.connectionId = connection.getId();
        this.myId = loggedInUserId;
        this.friendId = connection.getSender().equals(loggedInUserId)
                ? connection.getReceiver()
                : connection.getSender();
        this.userIsReceiver = connection.getSender().equals(loggedInUserId) ? false : true;
        this.createdAt = connection.getCreatedAt();
        this.lastMessageTimestamp = connection.getLastMessageTimestamp();
        this.numberOfMessages = connection.getNumberOfMessages();
        this.senderStatus = connection.getSenderStatus();
        this.receiverStatus = connection.getReceiverStatus();
    }
}
