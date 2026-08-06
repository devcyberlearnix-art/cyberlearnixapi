package com.example.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean logOtpValue;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.otp.log-value:true}") boolean logOtpValue) {
        this.mailSender = mailSender;
        this.logOtpValue = logOtpValue;
    }

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP for Admin Registration");
        message.setText("Your OTP is: " + otp + ". It expires in 10 minutes.");
        try {
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", toEmail);
            if (logOtpValue) {
                log.info("OTP value for {} is {}", toEmail, otp);
            }
        } catch (RuntimeException e) {
            log.error("Failed to send OTP email to {}", toEmail, e);
            throw e;
        }
    }
}