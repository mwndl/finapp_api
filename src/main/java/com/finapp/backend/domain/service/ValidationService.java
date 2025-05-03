package com.finapp.backend.domain.service;

import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class ValidationService {

    public void validateName(String name) {
        String nameRegex = "^(\\p{L}+\\s+\\p{L}+.*)$";
        Pattern pattern = Pattern.compile(nameRegex);
        Matcher matcher = pattern.matcher(name);

        if (!matcher.matches()) {
            throw new ApiException(ApiErrorCode.NAME_INVALID);
        }
    }

    public void validatePassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=])[A-Za-z\\d@$!%*?&#^()_+\\-=]{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);

        if (!matcher.matches()) {
            throw new ApiException(ApiErrorCode.PASSWORD_TOO_WEAK);
        }
    }

    public void validateUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9_]{4,}$";
        Pattern pattern = Pattern.compile(usernameRegex);
        Matcher matcher = pattern.matcher(username);

        if (!matcher.matches()) {
            throw new ApiException(ApiErrorCode.USERNAME_INVALID);
        }
    }
}
