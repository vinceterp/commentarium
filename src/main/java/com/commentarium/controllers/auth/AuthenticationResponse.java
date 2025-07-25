package com.commentarium.controllers.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String refreshToken;
    private String message;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String role;
    private String avatarUrl; // Optional: Include avatar URL if needed
    private Long userId; // Optional: Include user ID if needed for client-side operations
    private Boolean isEmailVerified; // Optional: Include email verification status if needed
}
