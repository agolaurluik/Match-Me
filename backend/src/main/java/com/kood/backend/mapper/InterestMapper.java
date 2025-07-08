package com.kood.backend.mapper;

import com.kood.backend.dto.EntityDTOs.InterestDTO;
import com.kood.backend.entity.Entities.Interest;

public class InterestMapper {
    public static InterestDTO toDTO(Interest interest) {
        if (interest == null) {
            return null;
        }
        return InterestDTO.builder()
                .id(interest.getId())
                .name(interest.getName())
                .build();
    }

    public static Interest toEntity(InterestDTO interestDTO) {
        if (interestDTO == null) {
            return null;
        }
        Interest interest = new Interest();
        interest.setId(interestDTO.getId());
        interest.setName(interestDTO.getName());
        return interest;
    }
}
