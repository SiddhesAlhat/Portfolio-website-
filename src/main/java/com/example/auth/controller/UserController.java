package com.example.auth.controller;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // Return a minimal profile without password
        record Profile(Long id, String username, String email, java.util.Set<String> roles, boolean enabled, java.time.Instant createdAt) {}
        Profile profile = new Profile(user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), user.isEnabled(), user.getCreatedAt());
        return ResponseEntity.ok(profile);
    }
}
