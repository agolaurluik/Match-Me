package com.kood.backend.dto.ConnectionDTOs;

import java.time.Instant;

import com.kood.backend.entity.ConnectionEntities.ConnectionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionDetailedDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Instant createdAt;
    private int numberOfMessages;
    private Instant lastMessageTimestamp;
    private ConnectionStatus senderStatus;
    private ConnectionStatus receiverStatus;
}