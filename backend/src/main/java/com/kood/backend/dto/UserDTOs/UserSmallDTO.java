package com.kood.backend.dto.UserDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserSmallDTO {

    private Long id;

    private String username;

    private String profileImageName;

}