package com.finapp.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
