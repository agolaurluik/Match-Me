package com.kood.backend.dto.LocationDTOs;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class LocationDTO {
    private Long id;
    private LocationPoint point;
    private double accuracy;
    private Instant timestamp;
}
