package backend.chat.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserStatusCheck {
    private Long userId;
    private Long meId;
}
