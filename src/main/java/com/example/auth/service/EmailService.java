package com.example.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationLink(String toEmail, String token) {
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Email Verification - Auth Service");
        message.setText("Please verify your email by clicking the link below:\n\n" + 
                       verificationUrl + "\n\n" +
                       "This link will expire in 24 hours.\n\n" +
                       "If you did not register for this account, please ignore this email.");
        
        mailSender.send(message);
    }

    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Verification Code - Auth Service");
        message.setText("Your verification code is: " + code + "\n\n" +
                       "This code will expire in 10 minutes.\n\n" +
                       "If you did not request this code, please ignore this email.");
        
        mailSender.send(message);
    }
}
