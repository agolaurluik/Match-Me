package com.kood.backend.dto.ChatDTOs;

import lombok.Data;

@Data
public class SendMessageDTO {
    private Long senderId;
    private Long receiverId;
    private String content;
}