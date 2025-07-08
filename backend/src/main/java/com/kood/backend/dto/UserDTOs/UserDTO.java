package com.kood.backend.dto.UserDTOs;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserDTO {

    private Long id;

    private String username;

    @Email(message = "Email format is invalid")
    private String email;

    private String passwordHash;

    private Instant birthDate;

    private Instant lastSeen;

    private Long genderId;

    private Long nationalityId;

    private Long purposeId;

    private Long locationId;

    private Long matchingFilterId;

    private Long namedLocationId;

    private String profileImageName;

    @Builder.Default
    private Set<Long> personalityIds = new HashSet<>();

    @Builder.Default
    private Set<Long> interestIds = new HashSet<>();

    private String userDescription;
}
