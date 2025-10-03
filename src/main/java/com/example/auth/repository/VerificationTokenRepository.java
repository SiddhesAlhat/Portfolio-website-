package com.example.auth.repository;

import com.example.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
    
    Optional<VerificationToken> findByEmailAndVerificationCodeAndUsedFalse(String email, String code);
    
    void deleteByExpiryDateBefore(Instant now);
}
