package com.finapp.backend.domain.repository;

import com.finapp.backend.domain.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
    List<LoginAttempt> findAllByIpAndUserAgentAndAttemptedAtAfter(String ip, String userAgent, LocalDateTime attemptedAt);
    void deleteAllByIpAndEmail(String ip, String email);
}