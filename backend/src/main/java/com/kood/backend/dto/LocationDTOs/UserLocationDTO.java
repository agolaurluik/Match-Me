package com.kood.backend.dto.LocationDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserLocationDTO {
    private Long id;
    private Long userId;
    private Long locationId;
}