package com.kood.backend.dto.UserDTOs;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserBioDTO {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Instant birthDate;

    private Long genderId;

    private Long nationalityId;

    private Long purposeId;

    private Long locationId;
    private Long namedLocationId;

    @Builder.Default
    private Set<Long> personalityIds = new HashSet<>();

    @Builder.Default
    private Set<Long> interestIds = new HashSet<>();
}
