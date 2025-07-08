package com.kood.backend.dto.algorithmDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMatchDetailDTO {
    private Long id;

    private Double distance;
    private int score;
    private int sharedInterests;
    private int sharedPersonalities;
}
