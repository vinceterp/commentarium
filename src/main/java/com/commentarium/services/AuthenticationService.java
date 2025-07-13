package com.commentarium.services;

import com.commentarium.controllers.auth.AuthenticationRequest;
import com.commentarium.controllers.auth.AuthenticationResponse;
import com.commentarium.controllers.auth.RegisterRequest;
import com.commentarium.entities.Role;
import com.commentarium.entities.Token;
import com.commentarium.entities.TokenType;
import com.commentarium.entities.User;
import com.commentarium.repositories.TokenRepository;
import com.commentarium.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository repository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final TokenRepository tokenRepository;

	public AuthenticationResponse register(RegisterRequest request) {

		try {
			if (repository.findByEmail(request.getEmail()).isPresent()) {
				throw new RuntimeException("Email already in use");
			}
			if (repository.findByUsername(request.getUsername()).isPresent()) {
				throw new RuntimeException("Username already in use");
			}
			var user = User.builder()
					.firstName(request.getFirstName())
					.lastName(request.getLastName())
					.email(request.getEmail())
					.password(passwordEncoder.encode(request.getPassword()))
					.username(request.getUsername())
					.role(request.getRole() != null ? request.getRole() : Role.USER)
					.build();
			var savedUser = repository.save(user);

			var jwtToken = jwtService.generateToken(user);

			var refreshToken = jwtService.generateRefreshToken(user);

			saveUserToken(savedUser, jwtToken);

			return AuthenticationResponse.builder()
					.token(jwtToken)
					.email(savedUser.getEmail())
					.firstName(savedUser.getFirstName())
					.lastName(savedUser.getLastName())
					.username(savedUser.getUsername())
					.role(savedUser.getRole().name())
					.message("User registered successfully")
					.refreshToken(refreshToken)
					.build();

		} catch (Exception e) {
			return AuthenticationResponse.builder()
					.message("Error during registration: " + e.getMessage())
					.build();
		}
	}

	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()));

			User user = repository.findByEmail(request.getEmail())
					.orElseThrow();

			var jwtToken = jwtService.generateToken(user);
			var refreshToken = jwtService.generateRefreshToken(user);
			revokeAllUserTokens(user);
			saveUserToken(user, jwtToken);
			return AuthenticationResponse.builder()
					.token(jwtToken)
					.refreshToken(refreshToken)
					.firstName(user.getFirstName())
					.lastName(user.getLastName())
					.email(user.getEmail())
					.username(user.getUsername())
					.role(user.getRole().name())
					.message("User authenticated successfully")
					.build();
		} catch (Exception e) {
			return AuthenticationResponse.builder()
					.message("Invalid credentials")
					.build();
		}
	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder()
				.user(user)
				.token(jwtToken)
				.tokenType(TokenType.BEARER)
				.expired(false)
				.revoked(false)
				.build();
		tokenRepository.save(token);
	}

	private void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}

	public void refreshToken(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String refreshToken;
		final String userEmail;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		refreshToken = authHeader.substring(7);
		userEmail = jwtService.extractUsername(refreshToken);
		if (userEmail != null) {
			var user = this.repository.findByEmail(userEmail)
					.orElseThrow();
			if (jwtService.isTokenValid(refreshToken, user)) {
				var accessToken = jwtService.generateToken(user);
				revokeAllUserTokens(user);
				saveUserToken(user, accessToken);
				var authResponse = AuthenticationResponse.builder()
						.token(accessToken)
						.refreshToken(refreshToken)
						.build();
				new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
			}
		}
	}

}
