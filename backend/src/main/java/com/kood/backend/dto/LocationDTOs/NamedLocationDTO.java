package com.kood.backend.dto.LocationDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class NamedLocationDTO {
    private Long id;
    private String name;
    private LocationPoint point;
    private double accuracy;
}
