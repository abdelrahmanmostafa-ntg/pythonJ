package com.example.demo;

import com.example.demo.controller.AuthController;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userRepository, passwordEncoder, emailService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void registerUser_Success() throws Exception {
        Mockito.when(userRepository.existsByUsername(anyString())).thenReturn(false);
        Mockito.when(userRepository.existsByEmail(anyString())).thenReturn(false);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(new User());

        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"test\",\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful. Please check your email for verification link."));
    }

    @Test
    void verifyEmail_Success() throws Exception {
        User user = new User();
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(Instant.now().plus(24, java.time.temporal.ChronoUnit.HOURS));

        Mockito.when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/verify")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully"));
    }

    @Test
    void verifyEmail_ExpiredToken() throws Exception {
        User user = new User();
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS));

        Mockito.when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/verify")
                .param("token", "expired-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Verification link has expired"));
    }

    @Test
    void resendVerification_Success() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setEmailVerified(false);

        Mockito.when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/resend-verification")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification email resent successfully"));
    }
}