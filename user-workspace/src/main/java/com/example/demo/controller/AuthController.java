package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Bucket loginBucket;
    private final EmailService emailService;

    public AuthController(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        this.loginBucket = Bucket4j.builder().addLimit(limit).build();
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS));
        userRepository.save(user);
        
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + user.getVerificationToken();
        emailService.sendVerificationEmail(user.getEmail(), verificationUrl);
        
        return ResponseEntity.ok("Registration successful. Please check your email for verification link.");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid verification token"));
            
        if (Instant.now().isAfter(user.getVerificationTokenExpiry())) {
            return ResponseEntity.badRequest().body("Verification link has expired");
        }
        
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Email not found"));
            
        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest().body("Email already verified");
        }
        
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS));
        userRepository.save(user);
        
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + user.getVerificationToken();
        emailService.sendVerificationEmail(user.getEmail(), verificationUrl);
        return ResponseEntity.ok("Verification email resent successfully");
    }
}