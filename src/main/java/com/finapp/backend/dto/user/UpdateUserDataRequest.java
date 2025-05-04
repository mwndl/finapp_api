package com.finapp.backend.dto.user;

import lombok.Data;

@Data
public class UpdateUserDataRequest {

    private String newName;
    private String newUsername;

}
