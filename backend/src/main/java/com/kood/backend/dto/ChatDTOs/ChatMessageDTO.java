package com.kood.backend.dto.ChatDTOs;

import java.time.Instant;

import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private Long id;

    private Long connectionId;

    private Long sender;

    private Long receiver;
    @Lob
    private String content;

    private String status; // SENT, READ

    private Instant timestamp; // ISO-8601 formatted string
}
