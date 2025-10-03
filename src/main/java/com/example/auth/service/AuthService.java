package com.example.auth.service;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.VerifyCodeRequest;
import com.example.auth.entity.User;
import com.example.auth.entity.VerificationToken;
import com.example.auth.repository.UserRepository;
import com.example.auth.repository.VerificationTokenRepository;
import com.example.auth.security.JwtUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Value("${app.verification.link-expiry-hours:24}")
    private int linkExpiryHours;

    @Value("${app.verification.code-expiry-minutes:10}")
    private int codeExpiryMinutes;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                      AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                      VerificationTokenRepository verificationTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String register(@Valid RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .verified(false)  // User starts unverified
                .build();

        userRepository.save(user);
        
        // Generate and send verification link
        sendVerificationLink(request.getEmail());
        
        return "User registered successfully. Please check your email to verify your account.";
    }

    @Transactional
    public void sendVerificationLink(String email) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(linkExpiryHours, ChronoUnit.HOURS);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .email(email)
                .expiryDate(expiryDate)
                .type("LINK")
                .build();

        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationLink(email, token);
    }

    @Transactional
    public void sendVerificationCode(String email) {
        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(codeExpiryMinutes, ChronoUnit.MINUTES);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .email(email)
                .verificationCode(code)
                .expiryDate(expiryDate)
                .type("CODE")
                .build();

        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public String verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Verification token already used");
        }

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        User user = userRepository.findByEmail(verificationToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        return "Email verified successfully. You can now login.";
    }

    @Transactional
    public String verifyCode(@Valid VerifyCodeRequest request) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByEmailAndVerificationCodeAndUsedFalse(request.getEmail(), request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification code has expired");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        return "Email verified successfully. You can now login.";
    }

    public AuthResponse login(@Valid LoginRequest request) {
        // First check if user exists and is verified
        User user = userRepository.findByUsername(request.getIdentifier())
                .or(() -> userRepository.findByEmail(request.getIdentifier()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isVerified()) {
            throw new IllegalArgumentException("Please verify your email before logging in");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
        );

        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        String token = jwtUtil.generateToken(principal.getUsername(), Map.of(
                "roles", principal.getAuthorities().stream().map(Object::toString).toList()
        ));

        long expiresAt = System.currentTimeMillis() + 3600000; // mirror default in JwtUtil

        return AuthResponse.builder()
                .token(token)
                .expiresAt(expiresAt)
                .username(principal.getUsername())
                .roles(principal.getAuthorities().stream().map(Object::toString).collect(java.util.stream.Collectors.toSet()))
                .build();
    }
}
