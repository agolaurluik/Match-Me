package backend.genericData.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class LocationPoint {
    private double longitude;
    private double latitude;
}
