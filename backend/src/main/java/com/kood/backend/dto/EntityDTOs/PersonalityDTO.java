package com.kood.backend.dto.EntityDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PersonalityDTO {
    private Long id;

    private String name;

}
