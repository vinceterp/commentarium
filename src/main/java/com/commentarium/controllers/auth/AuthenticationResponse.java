package com.commentarium.controllers.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse  {
    private String token;
    private String refreshToken;
    private String message;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String role;
}
