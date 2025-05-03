package com.finapp.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserDataRequest {

    @NotBlank(message = "Name is required")
    private String newName;
    private String newUsername;

}
