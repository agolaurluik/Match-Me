package com.kood.backend.dto.ChatDTOs;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TypingStatus {
    private String senderId;
    private String receiverId;
    private boolean typing;
}