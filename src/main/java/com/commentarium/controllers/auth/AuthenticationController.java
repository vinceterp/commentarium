package com.commentarium.controllers.auth;

import java.io.IOException;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commentarium.entities.CommentariumApiHelper;
import com.commentarium.services.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
    AuthenticationResponse response = service.register(request, true, null);
    if (response.getToken() == null) {
      return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(response);
    }
    return ResponseEntity.ok(response);
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
    AuthenticationResponse response = service.authenticate(request);
    if (response.getToken() == null) {
      return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(response);
    }
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/verify-email")
  public ResponseEntity<CommentariumApiHelper<String>> verifyEmail(@RequestBody EmailVerificationRequest request) {
    CommentariumApiHelper<String> response = service.verifyEmail(request);
    if (response.getData() == null) {
      return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(response);
    }
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh-token")
  public void refreshToken(
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    service.refreshToken(request, response);
  }
}
