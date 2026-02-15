package com.taxi.user.controller;

import com.taxi.user.dto.PaymentCardRequest;
import com.taxi.user.dto.UserRegistrationRequest;
import com.taxi.user.dto.UserResponse;
import com.taxi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{userId}/payment-card")
    public ResponseEntity<Void> addPaymentCard(
            @PathVariable Long userId,
            @Valid @RequestBody PaymentCardRequest request,
            Authentication authentication) {
        // Verify that userId matches authenticated user or user has ADMIN role
        String authenticatedEmail = authentication.getName();
        UserResponse authenticatedUser = userService.getUserByEmail(authenticatedEmail);

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!authenticatedUser.getId().equals(userId) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.addPaymentCard(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }
}
