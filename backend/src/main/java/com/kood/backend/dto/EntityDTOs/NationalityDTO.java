package com.kood.backend.dto.EntityDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class NationalityDTO {
    private Long id;

    private String name;

}
