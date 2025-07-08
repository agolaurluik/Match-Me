package com.kood.backend.dto.LocationDTOs;

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
