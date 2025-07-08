package com.kood.backend.dto.AuthDTOs;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
public class RegisterDTO {

    private Long id;

    @NotBlank(message = "Email is required")
    private String email;

    private String username;

    @Setter(AccessLevel.NONE)
    @NotBlank(message = "Password is required")
    private String password;

    private Instant birthDate;

    private Long genderId;

    private String profileImageName;
}