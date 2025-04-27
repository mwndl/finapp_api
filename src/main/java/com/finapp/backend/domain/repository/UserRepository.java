package com.finapp.backend.domain.repository;

import com.finapp.backend.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    int deleteByActiveFalseAndDeletionRequestedAtBefore(LocalDateTime dateTime);

}
