package com.kood.backend.dto.algorithmDTOs;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMatchDetailDTO {
    private Long id;
    private double matchPercentage;
    private Set<String> sharedInterestsSet;
    private Set<String> sharedPersonalitiesSet;
    private double distance;
    private String profileImageName;
}
