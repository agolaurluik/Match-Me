package com.kood.backend.dto.algorithmDTOs;

import java.time.Instant;
import java.util.Set;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserMatchDetailDTO {

    @NotNull
    private Long viewerId;

    @NotNull
    private Long candidateId;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double matchPercentage;

    @Size(max = 100)
    private Set<@Size(max = 100) String> sharedInterestsSet;

    @Size(max = 100)
    private Set<@Size(max = 100) String> sharedPersonalitiesSet;

    @PositiveOrZero
    private double distance;

    private String viewerProfileImageName;
    private String candidateProfileImageName;

    @NotNull
    private Instant createdAt;

    @NotNull
    @Size(min = 43, max = 43)
    private String signature;
}
