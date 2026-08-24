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

    /**
     * Sends an OTP email as part of the email-change verification flow.
     * The OTP expires in 5 minutes (per email-change configuration).
     *
     * @param toEmail the recipient address
     * @param otp     the 6-digit OTP to include in the email
     */
    public void sendEmailChangeOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CyberLearnix — Email Change Verification Code");
        message.setText("Your email change verification code is: " + otp
                + "\n\nThis code expires in 5 minutes."
                + "\n\nIf you did not request an email change, please contact support immediately."
                + "\n\n— The CyberLearnix Team");
        try {
            mailSender.send(message);
            log.info("[EmailChange] OTP email sent to {}", toEmail);
            if (logOtpValue) {
                log.info("[EmailChange] OTP value for {} is {}", toEmail, otp);
            }
        } catch (RuntimeException e) {
            log.error("[EmailChange] Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw e;
        }
    }

    /**
     * Sends a post-change security notification.
     * Failures are caught and logged by the caller — this method does NOT throw.
     *
     * @param toEmail  the recipient address
     * @param subject  email subject line
     * @param body     email body text
     */
    public void sendEmailChangeNotification(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
            log.info("[EmailChange] Notification email sent to {}", toEmail);
        } catch (Exception e) {
            // Non-transactional: caller logs the failure
            log.warn("[EmailChange] Could not send notification to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Sends an OTP code for a password change request.
     */
    public void sendPasswordChangeOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CyberLearnix — Password Change Verification Code");
        message.setText("Your password change verification code is: " + otp
                + "\n\nThis code is valid for 5 minutes."
                + "\n\nIf you did not request a password change, please secure your account immediately."
                + "\n\n— The CyberLearnix Team");
        try {
            mailSender.send(message);
            log.info("Password change OTP sent successfully to {}", toEmail);
            if (logOtpValue) {
                log.info("Password change OTP value for {} is {}", toEmail, otp);
            }
        } catch (RuntimeException e) {
            log.error("Failed to send password change OTP to {}: {}", toEmail, e.getMessage());
            throw e;
        }
    }

    /**
     * Sends a security alert notification indicating a successful password update.
     */
    public void sendPasswordChangeNotification(String toEmail, String timestampStr, String ipAddress, String deviceDetails) {
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
        try {
            mailSender.send(message);
            log.info("Password change security notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password change notification to {}: {}", toEmail, e.getMessage());
        }
    }
}