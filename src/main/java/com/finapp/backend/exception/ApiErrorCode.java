package com.finapp.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiErrorCode {
    // 400 - Bad Request
    PASSWORD_TOO_WEAK(400, "PASSWORD_TOO_WEAK", "Password does not meet complexity requirements", "Password must contain at least 8 characters, including uppercase, lowercase, number and special character"),
    NAME_INVALID(400, "NAME_INVALID", "Name format is invalid", "Name must contain at least two words with only letters"),
    SAME_NAME(400, "SAME_NAME", "Name is the same", "The new name must be different from the current one"),
    SAME_PASSWORD(400, "SAME_PASSWORD", "Password is the same", "The new password must be different from the current one"),

    INVALID_DESCRIPTION(400, "INVALID_DESCRIPTION", "Invalid description", "The description must be between 1 and 255 characters."),
    INVALID_TRANSACTION_TYPE(400, "INVALID_TRANSACTION_TYPE", "Invalid transaction type", "The specified transaction type is not recognized. Use ENTRY or EXIT." ),
    INVALID_DATE_FUTURE(400, "INVALID_DATE_FUTURE", "Date is invalid", "The date cannot be in the future"),
    INVALID_AMOUNT(400, "INVALID_AMOUNT", "Invalid amount", "The amount must be greater than 0"),
    MISSING_AMOUNT(400, "MISSING_AMOUNT", "Amount is required", "The amount must be provided and greater than 0"),
    MISSING_DATE(400, "MISSING_DATE", "Date is required", "The date must be provided"),
    MISSING_TRANSACTION_TYPE(400, "MISSING_TRANSACTION_TYPE", "Transaction type is required", "The transaction type must be provided"),
    VALIDATION_ERROR(400, "VALIDATION_ERROR", "Validation failed", "One or more fields did not pass validation"),
    COLLABORATOR_ALREADY_EXISTS(400, "COLLABORATOR_ALREADY_EXISTS", "Collaborator already added", "The user is already a collaborator of this fund box."),
    COLLABORATOR_CANNOT_BE_OWNER(400, "COLLABORATOR_CANNOT_BE_OWNER", "Invalid collaborator", "The owner of a fund box cannot be added as a collaborator."),

    // 401 - Unauthorized
    UNAUTHENTICATED(401, "UNAUTHENTICATED", "User not authenticated", "Authentication is required to access this resource. Please provide a valid token."),
    AUTH_EMAIL_NOT_FOUND(401, "AUTH_EMAIL_NOT_FOUND", "Email not found", "The email provided was not found in the database."),
    AUTH_INVALID_TOKEN(401, "AUTH_INVALID_TOKEN", "Invalid token" , "The token provided is invalid" ),
    EXPIRED_SESSION(401, "EXPIRED_SESSION", "Session expired", "Your session has expired. Please log in again to continue."),
    INVALID_CREDENTIALS(401, "INVALID_CREDENTIALS", "Invalid credentials", "Email or password is incorrect."),
    ACCOUNT_DEACTIVATED(401, "ACCOUNT_DEACTIVATED", "Account is deactivated", "Your account is in the process of being deleted. Please log in again to reactivate your account."),

    // 403 - Forbidden
    UNAUTHORIZED_ACCESS(403, "UNAUTHORIZED_ACCESS", "Unauthorized access", "You are not authorized to access this resource."),
    ACCOUNT_LOCKED(403, "ACCOUNT_LOCKED", "Account is locked", "Your account is locked due to too many failed login attempts"),

    // 404 - Not Found
    DEPOSIT_NOT_FOUND(404, "DEPOSIT_NOT_FOUND", "Deposit not found", "The requested deposit was not found"),
    COLLABORATOR_NOT_FOUND(404, "COLLABORATOR_NOT_FOUND", "Collaborator not found", "The specified collaborator does not exist in this fund box."),
    FUND_BOX_NOT_FOUND(404, "FUND_BOX_NOT_FOUND", "FundBox not found", "Unable to find requested FundBox"),
    USER_NOT_FOUND(404, "USER_NOT_FOUND", "User not found", "The user was not found in the database."),

    // 409 - Conflict
    FUND_BOX_NAME_ALREADY_EXISTS(409, "FUND_BOX_NAME_ALREADY_EXISTS", "Fundbox name must be unique", "The name provided is already in use, each fundbox must have a unique name per user"),
    EMAIL_ALREADY_REGISTERED(409, "EMAIL_ALREADY_REGISTERED", "Email already registered", "An account with this email already exists."),

    // 429 - Too Many Requests
    TOO_MANY_REQUESTS(429, "TOO_MANY_REQUESTS", "Too many requests", "You have exceeded the number of allowed requests. Please try again later."),

    // Server Error
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "Unexpected error", "An unexpected error occurred"),
    SERVICE_UNAVAILABLE(503, "SERVICE_UNAVAILABLE", "Service unavailable", "The service is temporarily unavailable. Please try again later");

    private final int httpStatus;
    private final String code;
    private final String title;
    private final String description;

    ApiErrorCode(int httpStatus, String code, String title, String description) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.title = title;
        this.description = description;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.valueOf(this.httpStatus);
    }
}
