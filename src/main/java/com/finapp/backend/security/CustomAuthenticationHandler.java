package com.finapp.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiErrorResponse;
import com.finapp.backend.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ApiException apiException = new ApiException(ApiErrorCode.UNAUTHENTICATED);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                apiException.getErrorCode().name(),
                apiException.getErrorCode().getTitle(),
                apiException.getErrorCode().getDescription()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
