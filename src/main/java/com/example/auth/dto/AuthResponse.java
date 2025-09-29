package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private long expiresAt;
    private String username;
    private Set<String> roles;
}
