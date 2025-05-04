package com.finapp.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserSearchResult {
    UUID id;
    String username;
    String name;
    double confidence;
}
