package com.commentarium.config;

import com.commentarium.repositories.TokenRepository;
import com.commentarium.services.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final TokenRepository tokenRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    String path = request.getServletPath();
    String method = request.getMethod();

    if (path.contains("/api/v1/auth") ||
        path.contains("/api/v1/health") ||
        (path.equals("/api/v1/posts") && method.equals("GET"))) {
      filterChain.doFilter(request, response);
      return;
    }

    final String jwtToken = extractToken(request);
    final String userEmail;

    if (jwtToken == null) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.getWriter().write("JWT token is missing");
      response.getWriter().flush();
      response.getWriter().close();
      return;
    }
    try {
      userEmail = jwtService.extractUsername(jwtToken);
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
        var isTokenValid = tokenRepository.findByToken(jwtToken)
            .map(t -> !t.isExpired() && !t.isRevoked())
            .orElse(false);
        if (jwtService.isTokenValid(jwtToken, userDetails) && isTokenValid) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());
          authToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("JWT token is expired");
      response.getWriter().flush();
      response.getWriter().close();
      return;
    }

  }

  private String extractToken(HttpServletRequest request) {
    // Extract the token from the Authorization header
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}
