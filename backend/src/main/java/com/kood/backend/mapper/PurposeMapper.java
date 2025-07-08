package com.kood.backend.mapper;

import com.kood.backend.dto.EntityDTOs.PurposeDTO;
import com.kood.backend.entity.Entities.Purpose;

public interface PurposeMapper {
    public static PurposeDTO toDTO(Purpose purpose) {
        if (purpose == null) {
            return null;
        }
        return PurposeDTO.builder()
                .id(purpose.getId())
                .name(purpose.getName())
                .build();
    }

    public static Purpose toEntity(PurposeDTO purposeDTO) {
        if (purposeDTO == null) {
            return null;
        }
        Purpose purpose = new Purpose();
        purpose.setId(purposeDTO.getId());
        purpose.setName(purposeDTO.getName());
        return purpose;
    }
}