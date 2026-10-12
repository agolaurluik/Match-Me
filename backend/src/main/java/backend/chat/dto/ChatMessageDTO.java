package backend.chat.dto;

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

    private Long messageId;

    private Long connectionId;

    private Long senderId;

    private Long receiverId;
    @Lob
    private String content;

    private String status; // SENT, READ

    private Instant timestamp; // ISO-8601 formatted string
}
