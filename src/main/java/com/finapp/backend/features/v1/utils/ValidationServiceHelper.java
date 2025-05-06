package com.finapp.backend.features.v1.utils;

import com.finapp.backend.features.v1.user.repository.UserRepository;
import com.finapp.backend.shared.exception.ApiErrorCode;
import com.finapp.backend.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
public class ValidationServiceHelper {

    private final UserRepository userRepository;

    public void validateName(String name) {
        String nameRegex = "^[A-Za-zÀ-ÿ]+\\s+[A-Za-zÀ-ÿ]+(\\s+[A-Za-zÀ-ÿ]+)*$";
        Pattern pattern = Pattern.compile(nameRegex);
        Matcher matcher = pattern.matcher(name);

        if (!matcher.matches()) throw new ApiException(ApiErrorCode.NAME_INVALID);
    }

    public void validatePassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=])[A-Za-z\\d@$!%*?&#^()_+\\-=]{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);

        if (!matcher.matches()) throw new ApiException(ApiErrorCode.PASSWORD_TOO_WEAK);
    }

    private static final Set<String> RESERVED_USERNAMES = Set.of("admin", "root", "support", "api", "user", "null", "me", "system");
    public void validateUsername(String username) {

        String usernameRegex = "^(?!.*[._-]{2})(?![._-])[a-z0-9._-]{4,15}(?<![._-])$";
        Pattern pattern = Pattern.compile(usernameRegex);
        Matcher matcher = pattern.matcher(username);

        if (!matcher.matches()) throw new ApiException(ApiErrorCode.USERNAME_INVALID);
        if (RESERVED_USERNAMES.contains(username)) throw new ApiException(ApiErrorCode.USERNAME_RESERVED);
        if (userRepository.existsByUsername(username)) throw new ApiException(ApiErrorCode.USERNAME_ALREADY_TAKEN);
    }
}
