package com.commentarium.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import com.commentarium.repositories.TokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("No JWT token provided");
                return;
            }
            jwt = authHeader.substring(7);
            var storedToken = tokenRepository.findByToken(jwt)
                    .orElse(null);

            if (storedToken != null) {
                if (storedToken.expired || storedToken.revoked) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Token already expired or revoked");
                    return;
                }
                storedToken.setExpired(true);
                storedToken.setRevoked(true);
                tokenRepository.save(storedToken);
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {

        }
    }
}
