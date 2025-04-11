package com.finapp.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {
    private final String errorCode;
    private final String title;
    private final String description;
}
