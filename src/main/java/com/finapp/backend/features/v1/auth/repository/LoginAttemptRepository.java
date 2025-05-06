package com.finapp.backend.features.v1.auth.repository;

import com.finapp.backend.domain.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
    void deleteAllByIpAndEmail(String ip, String email);

    Optional<LoginAttempt> findByIpAndUserAgentAndEmail(String ip, String userAgent, String email);
}