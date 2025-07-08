package com.kood.backend.mapper;

import com.kood.backend.dto.EntityDTOs.GenderDTO;
import com.kood.backend.entity.Entities.Gender;

public class GenderMapper {
    public static GenderDTO toDTO(Gender gender) {
        if (gender == null) {
            return null;
        }
        return GenderDTO.builder()
                .id(gender.getId())
                .name(gender.getName())
                .build();
    }

    public static Gender toEntity(GenderDTO genderDTO) {
        if (genderDTO == null) {
            return null;
        }
        Gender gender = new Gender();
        gender.setId(genderDTO.getId());
        gender.setName(genderDTO.getName());
        return gender;
    }
}
