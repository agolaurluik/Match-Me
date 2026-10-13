package backend.chat.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ConnectionReadStatusUpdateDTO {
    private List<Long> seenConnectionIds;
}
