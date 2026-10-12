package backend.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserMeProfileDTO {
    Long id;
    String userDescription;
    String email;
}