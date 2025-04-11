package com.finapp.backend.controller;

import com.finapp.backend.dto.user.UpdateUserRequest;
import com.finapp.backend.dto.user.UserResponse;
import com.finapp.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        userService.updateUser(userDetails.getUsername(), request);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping
    public ResponseEntity<?> requestAccountDeletion(@AuthenticationPrincipal UserDetails userDetails) {
        userService.requestAccountDeletion(userDetails.getUsername());
        return ResponseEntity.ok("Account deletion requested. You can revert by logging in within 30 days.");
    }
}
