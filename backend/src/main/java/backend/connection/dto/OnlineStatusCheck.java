package backend.connection.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OnlineStatusCheck {
    private String userId;
    private List<String> friendIds;
}
