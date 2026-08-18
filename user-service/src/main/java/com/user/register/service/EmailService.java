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

    /**
     * Sends a notification email (non-OTP) to the given address.
     * Used by {@code EmailChangeService} to inform both the old and new email
     * addresses about a successful or suspicious email change.
     *
     * @param toEmail the recipient email address
     * @param subject the email subject line
     * @param body    the plain-text email body
     */
    public void sendEmailChangeNotification(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Email change notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending email change notification to {}: {}", toEmail, e.getMessage());
            // Do not rethrow — notification failure must not roll back the email change transaction.
        }
    }

    /**
     * Sends an OTP code for a password change request.
     */
    public void sendPasswordChangeOtp(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("CyberLearnix — Password Change Verification Code");
            message.setText("Your password change verification code is: " + otp
                    + "\n\nThis code is valid for 5 minutes."
                    + "\n\nIf you did not request a password change, please secure your account immediately."
                    + "\n\n— The CyberLearnix Team");
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Password change OTP sent successfully to {}", toEmail);
            if (logOtpValue) {
                log.info("Password change OTP value for {} is {}", toEmail, otp);
            }
        } catch (Exception e) {
            log.error("Error sending password change OTP to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Unable to send OTP email right now. Please try again later.");
        }
    }

    /**
     * Sends a security alert notification indicating a successful password update.
     */
    public void sendPasswordChangeNotification(String toEmail, String timestampStr, String ipAddress, String deviceDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Security Alert — Your CyberLearnix password has been changed");
            message.setText("Hi,\n\n"
                    + "This is a security notification to confirm that the password for your CyberLearnix account has been changed.\n\n"
                    + "Activity Details:\n"
                    + "• Date/Time: " + timestampStr + "\n"
                    + "• IP Address: " + ipAddress + "\n"
                    + "• Device/Browser: " + deviceDetails + "\n\n"
                    + "If you did not make this change, please contact support immediately to lock your account.\n\n"
                    + "— The CyberLearnix Team");
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Password change security notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password change notification to {}: {}", toEmail, e.getMessage());
            // Do not throw to prevent rolling back password update transaction
        }
    }
}