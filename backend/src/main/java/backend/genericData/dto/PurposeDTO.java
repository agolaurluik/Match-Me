package backend.genericData.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PurposeDTO {
    private Long id;

    private String name;

}
