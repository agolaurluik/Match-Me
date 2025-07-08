package com.kood.backend.mapper;

import com.kood.backend.dto.EntityDTOs.PersonalityDTO;
import com.kood.backend.entity.Entities.Personality;

public interface PersonalityMapper {
    public static PersonalityDTO toDTO(Personality Personality) {
        if (Personality == null) {
            return null;
        }
        return PersonalityDTO.builder()
                .id(Personality.getId())
                .name(Personality.getName())
                .build();
    }

    public static Personality toEntity(PersonalityDTO PersonalityDTO) {
        if (PersonalityDTO == null) {
            return null;
        }
        Personality Personality = new Personality();
        Personality.setId(PersonalityDTO.getId());
        Personality.setName(PersonalityDTO.getName());
        return Personality;
    }
}