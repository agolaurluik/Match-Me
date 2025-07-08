package com.kood.backend.dto.algorithmDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMatchingFilterDTO {
    private Long id;
    private Long userId;
    private Integer match_limit;
    private String genderPreference;
    private Integer nationalityScore;
    private Integer interestScore;
    private Integer personalityScore;
    private Integer purposeScore;
    private Integer highestAge;
    private Integer lowestAge;
    private Integer radius;
}
