package com.commentarium.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commentarium.entities.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByEmailAndToken(String email, String token);

    boolean deleteByEmail(String email);
}
