package com.commentarium.services;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.commentarium.controllers.auth.AuthenticationRequest;
import com.commentarium.controllers.auth.AuthenticationResponse;
import com.commentarium.controllers.auth.EmailVerificationRequest;
import com.commentarium.controllers.auth.RegisterRequest;
import com.commentarium.entities.CommentariumApiHelper;
import com.commentarium.entities.Role;
import com.commentarium.entities.Token;
import com.commentarium.entities.User;
import com.commentarium.entities.VerificationToken;
import com.commentarium.repositories.TokenRepository;
import com.commentarium.repositories.UserRepository;
import com.commentarium.repositories.VerificationTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final TokenRepository tokenRepository;
	private final VerificationTokenRepository verificationTokenRepository;
	private final EmailService emailService;

	public CommentariumApiHelper<String> verifyEmail(EmailVerificationRequest request) {
		try {
			if (request.getEmail() == null || request.getVerificationCode() == null) {
				throw new RuntimeException("Email and verification code must be provided");
			}

			User user = userRepository.findByEmail(request.getEmail())
					.orElseThrow(() -> new RuntimeException("User not found"));

			if (user.isEmailVerified()) {
				throw new RuntimeException("Email is already verified");
			}

			VerificationToken code = verificationTokenRepository
					.findByEmailAndToken(request.getEmail(), request.getVerificationCode())
					.orElseThrow(() -> new RuntimeException("Invalid verification code"));

			if (code.getCreatedAt().before(new Date(System.currentTimeMillis() - 15 * 60 * 1000))) {
				throw new RuntimeException("Verification code has expired");
			}

			verificationTokenRepository.deleteById(code.getId());
			user.setEmailVerified(true);
			userRepository.save(user);

			return CommentariumApiHelper.<String>builder()
					.message("Email verified successfully")
					.data(new StringBuilder()
							.append("Email verified: ").append(request.getEmail())
							.toString())
					.build();
		} catch (Exception e) {
			return CommentariumApiHelper.<String>builder()
					.message("Error during email verification: " + e.getMessage())
					.data(null)
					.status("failure")
					.build();
		}
	}

	public AuthenticationResponse register(RegisterRequest request, boolean withEmailVerification, Role role) {

		try {
			if (userRepository.findByEmail(request.getEmail()).isPresent()) {
				throw new RuntimeException("Email already in use");
			}
			if (userRepository.findByUsername(request.getUsername()).isPresent()) {
				throw new RuntimeException("Username already in use");
			}

			if (withEmailVerification) {
				VerificationToken verificationToken = VerificationToken.builder()
						.email(request.getEmail())
						.token(generateVerificationCode())
						.createdAt(new Date(System.currentTimeMillis()))
						.build();
				verificationTokenRepository.save(verificationToken);
				SimpleMailMessage emailMessage = new SimpleMailMessage();
				emailMessage.setFrom("no-reply@commentarium.xyz");
				emailMessage.setTo(request.getEmail());
				emailMessage.setSubject("Email Verification");
				emailMessage.setText("Your verification code is: " + verificationToken.getToken());
				emailService.sendEmail(emailMessage);
			}

			var user = User.builder()
					.firstName(request.getFirstName())
					.lastName(request.getLastName())
					.email(request.getEmail())
					.password(passwordEncoder.encode(request.getPassword()))
					.username(request.getUsername())
					.role(role != null ? role : Role.USER)
					.isEmailVerified(!withEmailVerification) // Set to true if no email verification is needed
					.build();
			var savedUser = userRepository.save(user);

			// var jwtToken = jwtService.generateToken(user);

			// var refreshToken = jwtService.generateRefreshToken(user);

			// saveUserToken(savedUser, jwtToken);

			return AuthenticationResponse.builder()
					// .token(jwtToken)
					.email(savedUser.getEmail())
					.firstName(savedUser.getFirstName())
					.lastName(savedUser.getLastName())
					.username(savedUser.getUsername())
					.role(savedUser.getRole().name())
					.userId(savedUser.getId())
					.message("User registered successfully")
					.isEmailVerified(user.isEmailVerified())
					// .refreshToken(refreshToken)
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

			User user = userRepository.findByEmail(request.getEmail())
					.orElseThrow();

			if (!user.isEmailVerified()) {
				throw new RuntimeException("Email not verified");
			}
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
					.userId(user.getId())
					.message("User authenticated successfully")
					.build();
		} catch (Exception e) {
			return AuthenticationResponse.builder()
					.message(e.getMessage())
					.build();
		}
	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder()
				.user(user)
				.token(jwtToken)
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

	private String generateVerificationCode() {
		// generate a random string of 6 numbers
		return String.valueOf((int) (Math.random() * 900000) + 100000);
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
			var user = userRepository.findByEmail(userEmail)
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
