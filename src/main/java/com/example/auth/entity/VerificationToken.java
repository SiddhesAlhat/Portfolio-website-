package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private String type; // "LINK" or "CODE"

    private boolean used = false;

    private Instant createdAt = Instant.now();
}
