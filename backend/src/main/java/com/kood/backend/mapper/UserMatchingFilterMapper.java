package com.kood.backend.mapper;

import com.kood.backend.dto.algorithmDTOs.UserMatchingFilterDTO;
import com.kood.backend.entity.UserEntities.MatchingFilter;

public interface UserMatchingFilterMapper {
    UserMatchingFilterDTO toDTO(MatchingFilter entity);

    MatchingFilter toEntity(UserMatchingFilterDTO dto);
}