package com.kood.backend.dto.UserDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserProfileDTO {
    Long id;
    String userDescription;
}
