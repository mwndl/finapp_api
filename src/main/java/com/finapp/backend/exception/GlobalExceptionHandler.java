package com.finapp.backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        ApiErrorCode code = ex.getErrorCode();
        ApiErrorResponse response = new ApiErrorResponse(
                code.getCode(),
                code.getTitle(),
                code.getDescription()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            ApiErrorCode errorCode = switch (fieldError.getField()) {
                case "password" -> ApiErrorCode.PASSWORD_TOO_WEAK;
                case "name" -> ApiErrorCode.NAME_INVALID;
                default -> ApiErrorCode.VALIDATION_ERROR;
            };

            ApiErrorResponse response = new ApiErrorResponse(
                    errorCode.getCode(),
                    errorCode.getTitle(),
                    errorCode.getDescription()
            );

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        ApiErrorCode fallback = ApiErrorCode.VALIDATION_ERROR;
        ApiErrorResponse response = new ApiErrorResponse(
                fallback.getCode(),
                fallback.getTitle(),
                fallback.getDescription()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                ApiErrorCode.VALIDATION_ERROR.getCode(),
                "Constraint violation",
                ex.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                ApiErrorCode.INTERNAL_ERROR.getCode(),
                "Unexpected error",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
