package backend.connection.mapper;

import backend.connection.dto.ConnectionDTO;
import backend.connection.dto.ConnectionDetailedDTO;
import backend.connection.dto.ConnectionUpdateNotification;
import backend.connection.dto.UserStatusCheck;
import backend.connection.entity.Connection;
import backend.matching.dto.UserMatchDetailDTO;

public class ConnectionMapper {

    public static ConnectionDetailedDTO toDetailedDTO(Connection connection) {
        return ConnectionDetailedDTO.builder()
                .id(connection.getId())
                .senderId(connection.getSender())
                .receiverId(connection.getReceiver())
                .senderStatus(connection.getSenderStatus())
                .receiverStatus(connection.getReceiverStatus())
                .createdAt(connection.getCreatedAt())
                .build();
    }

    public static UserMatchDetailDTO toUserMatchDetailDTO(Connection connection) {
        return UserMatchDetailDTO.builder()
                .senderId(connection.getSender())
                .receiverId(connection.getReceiver())
                .matchPercentage(connection.getMatchPercentage())
                .sharedInterestsSet(connection.getSharedInterestsSet())
                .sharedPersonalitiesSet(connection.getSharedPersonalitiesSet())
                .distance(connection.getDistance())
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