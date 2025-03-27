package com.example.demo.service;

public interface EmailService {
    void sendVerificationEmail(String to, String verificationUrl);
}