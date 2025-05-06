package com.finapp.backend.shared.security;

import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.enums.UserStatus;
import com.finapp.backend.features.v1.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER") // can be adapted in the future
                .accountLocked(
                        user.getStatus() == UserStatus.DEACTIVATION_REQUESTED || user.getStatus() == UserStatus.LOCKED
                )
                .build();
    }
}
