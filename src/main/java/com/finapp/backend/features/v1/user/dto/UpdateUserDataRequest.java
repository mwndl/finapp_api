package com.finapp.backend.features.v1.user.dto;

import lombok.Data;

@Data
public class UpdateUserDataRequest {

    private String newName;
    private String newUsername;

}
