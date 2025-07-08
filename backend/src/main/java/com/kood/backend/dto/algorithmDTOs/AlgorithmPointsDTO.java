package com.kood.backend.dto.algorithmDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AlgorithmPointsDTO {
    private Integer match_limit;
    private String genderPreference;
    private Integer nationalityScore;
    private Integer interestScore;
    private Integer personalityScore;
    private Integer purposeScore;
    private Integer highestAge;
    private Integer lowestAge;
}
