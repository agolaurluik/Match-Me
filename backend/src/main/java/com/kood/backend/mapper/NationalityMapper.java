package com.kood.backend.mapper;

import com.kood.backend.dto.EntityDTOs.NationalityDTO;
import com.kood.backend.entity.Entities.Nationality;

public class NationalityMapper {
    public static NationalityDTO toDTO(Nationality nationality) {
        if (nationality == null) {
            return null;
        }
        return NationalityDTO.builder()
                .id(nationality.getId())
                .name(nationality.getName())
                .build();
    }

    public static Nationality toEntity(NationalityDTO nationalityDTO) {
        if (nationalityDTO == null) {
            return null;
        }
        Nationality nationality = new Nationality();
        nationality.setId(nationalityDTO.getId());
        nationality.setName(nationalityDTO.getName());
        return nationality;
    }
}
