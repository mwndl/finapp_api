package com.finapp.backend.shared.security;

import com.finapp.backend.domain.model.UserToken;
import com.finapp.backend.domain.repository.UserTokenRepository;
import com.finapp.backend.shared.exception.ApiErrorCode;
import com.finapp.backend.shared.exception.ApiException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserTokenRepository userTokenRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String userEmail = jwtUtil.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (!jwtUtil.isTokenValid(jwt, userDetails)) {
                    handlerExceptionResolver.resolveException(request, response, null,
                            new ApiException(ApiErrorCode.INVALID_ACCESS_TOKEN));
                    return;
                }

                Optional<UserToken> userTokenOpt = userTokenRepository.findByAccessTokenAndRevokedFalse(jwt);
                if (userTokenOpt.isEmpty() || jwtUtil.isTokenExpired(jwt)) {
                    handlerExceptionResolver.resolveException(request, response, null,
                            new ApiException(ApiErrorCode.EXPIRED_SESSION));
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (ExpiredJwtException e) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new ApiException(ApiErrorCode.EXPIRED_SESSION));
            return;

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new ApiException(ApiErrorCode.INVALID_ACCESS_TOKEN));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
