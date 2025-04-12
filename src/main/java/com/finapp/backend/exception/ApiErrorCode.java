package com.finapp.backend.exception;

import lombok.Getter;

@Getter
public enum ApiErrorCode {

    // validation
    PASSWORD_TOO_WEAK("PASSWORD_TOO_WEAK", "Password does not meet complexity requirements", "Password must contain at least 8 characters, including uppercase, lowercase, number and special character"),
    NAME_INVALID("NAME_INVALID", "Name format is invalid", "Name must contain at least two words with only letters"),
    NAME_UNCHANGED("NAME_UNCHANGED", "New name must be different from current one", "You must provide a name that is not equal to your current name"),
    PASSWORD_UNCHANGED("PASSWORD_UNCHANGED", "New password must be different from current one", "You must provide a password different from the current one"),

    // auth
    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED", "Email already registered", "An account with this email already exists."),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid credentials", "Email or password is incorrect."),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", "The user was not found in the system."),

    // user
    ACCOUNT_DEACTIVATED("ACCOUNT_DEACTIVATED", "Account is deactivated", "Your account is in the process of being deleted. Please log in again to reactivate your account."),
    SAME_NAME("SAME_NAME", "Name is the same", "The new name must be different from the current one"),
    SAME_PASSWORD("SAME_PASSWORD", "Password is the same", "The new password must be different from the current one"),

    // deposit
    MISSING_AMOUNT("MISSING_AMOUNT", "Amount is required", "The amount must be provided and greater than 0"),
    INVALID_AMOUNT("INVALID_AMOUNT", "Invalid amount", "The amount must be greater than 0"),
    MISSING_DATE("MISSING_DATE", "Date is required", "The date must be provided"),
    MISSING_TRANSACTION_TYPE("MISSING_TRANSACTION_TYPE", "Transaction type is required", "The transaction type must be provided"),
    DEPOSIT_NOT_FOUND("DEPOSIT_NOT_FOUND", "Deposit not found", "The requested deposit was not found"),
    // generic
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed", "One or more fields did not pass validation"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Unexpected error", "An unexpected error occurred"),
    FUND_BOX_NOT_FOUND("FUND_BOX_NOT_FOUND", "FundBox not found", "Unable to find requested FundBox");


    private final String code;
    private final String title;
    private final String description;

    ApiErrorCode(String code, String title, String description) {
        this.code = code;
        this.title = title;
        this.description = description;
    }
}
