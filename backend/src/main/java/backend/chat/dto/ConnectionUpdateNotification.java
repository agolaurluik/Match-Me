package backend.chat.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ConnectionUpdateNotification {
    private Long userId;
}
