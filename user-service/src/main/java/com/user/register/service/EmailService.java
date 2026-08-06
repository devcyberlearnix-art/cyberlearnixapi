package com.user.register.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.otp.log-value:true}")
    private boolean logOtpValue;

    // Generate 6-digit OTP
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Send OTP Email
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp);
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("OTP sent successfully to {}", toEmail);
            if (logOtpValue) {
                log.info("OTP value for {} is {}", toEmail, otp);
            }
        } catch (Exception e) {
            log.error("Error sending OTP to {}", toEmail, e);
            throw new RuntimeException("Unable to send OTP email right now. Please try again later.");
        }
    }
}