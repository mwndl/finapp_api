package com.finapp.backend.domain.repository;

import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    int deleteByStatusAndDeletionRequestedAtBefore(UserStatus status, LocalDateTime dateTime);

}
