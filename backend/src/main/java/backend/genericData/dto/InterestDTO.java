package backend.genericData.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class InterestDTO {
    private Long id;

    private String name;

}
