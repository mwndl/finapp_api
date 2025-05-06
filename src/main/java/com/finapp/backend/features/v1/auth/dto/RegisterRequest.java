package com.finapp.backend.features.v1.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Username cannot be blank")
    private String Username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
