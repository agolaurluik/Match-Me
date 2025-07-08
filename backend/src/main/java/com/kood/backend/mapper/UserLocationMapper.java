package com.kood.backend.mapper;

import com.kood.backend.dto.LocationDTOs.UserLocationDTO;
import com.kood.backend.entity.UserEntities.UserLocation;

public interface UserLocationMapper {
    UserLocation toEntity(UserLocationDTO UserLocationDTO);

    UserLocationDTO toDTO(UserLocation user);
}
