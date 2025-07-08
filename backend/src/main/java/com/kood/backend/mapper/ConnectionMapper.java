package com.kood.backend.mapper;

import com.kood.backend.dto.ConnectionDTOs.ConnectionDTO;
import com.kood.backend.dto.ConnectionDTOs.ConnectionDetailedDTO;
import com.kood.backend.dto.ConnectionDTOs.ConnectionUpdateNotification;
import com.kood.backend.dto.ConnectionDTOs.UserStatusCheck;
import com.kood.backend.entity.ConnectionEntities.Connection;

public class ConnectionMapper {

    public static ConnectionDetailedDTO toDetailedDTO(Connection connection, String lastMessagePreview) {
        return ConnectionDetailedDTO.builder()
                .id(connection.getId())
                .senderId(connection.getSender())
                .receiverId(connection.getReceiver())
                .senderStatus(connection.getSenderStatus())
                .receiverStatus(connection.getReceiverStatus())
                .createdAt(connection.getCreatedAt())
                .build();
    }

    public static ConnectionDTO toDTO(Connection connection) {
        if (connection == null) {
            return null;
        }
        return ConnectionDTO.builder()
                .id(connection.getId())
                .senderId(connection.getSender())
                .receiverId(connection.getReceiver())
                .build();
    }

    public static Connection toEntity(ConnectionDetailedDTO connectionDTO) {
        if (connectionDTO == null) {
            return null;
        }
        Connection connection = new Connection();
        connection.setId(connectionDTO.getId());
        connection.setSender(connectionDTO.getSenderId());
        connection.setReceiver(connectionDTO.getReceiverId());
        connection.setLastMessageTimestamp(connectionDTO.getLastMessageTimestamp());
        connection.setSenderStatus((connectionDTO.getSenderStatus()));
        connection.setReceiverStatus((connectionDTO.getReceiverStatus()));
        connection.setCreatedAt(connectionDTO.getCreatedAt());
        return connection;
    }

    public static ConnectionUpdateNotification toUpdateNotification(UserStatusCheck payload) {
        if (payload == null) {
            return null;
        }

        return ConnectionUpdateNotification.builder()
                .userId(payload.getMeId())
                .build();
    }

}