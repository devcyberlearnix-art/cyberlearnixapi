package com.user.register.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Optional;
import java.util.Random;

import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.util.SecurityUtils;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Value("${spring.mail.username:noreply@cyberlearnix.com}")
    private String fromEmail;

    @Value("${app.otp.log-value:true}")
    private boolean logOtpValue;

    @Value("${app.encryption-key:1234567890123456}")
    private String encryptionKey;

    // ─── Username extraction from email ────────────────────────────────────────
    private String resolveUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            String username = email.substring(0, email.indexOf('@')).trim();
            if (!username.isEmpty()) {
                return username;
            }
        }
        return "User";
    }

    // ─── OTP generation ────────────────────────────────────────────────────────

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // ─── Public OTP email methods ───────────────────────────────────────────────

    /** Sends a Registration OTP email. */
    public void sendOtpEmail(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Registration OTP Verification",
                "EMAIL VERIFICATION OTP",
                "Welcome to CyberLearnix!",
                "Thank you for registering with us. Please verify your email address using the OTP below to activate your account.",
                "5",
                "Verifying your email helps us keep your account secure and fully activated."
        );
    }

    /** Sends a Login Verification OTP email. */
    public void sendLoginOtp(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Login Verification",
                "LOGIN VERIFICATION OTP",
                "We detected a login attempt to your CyberLearnix account from a new device or location.",
                "Please use the One-Time Password (OTP) below to verify your identity and continue logging in.",
                "5",
                "If you didn't attempt to log in, please ignore this email. Your account is secure."
        );
    }

    /** Sends a Password Reset OTP email (Forgot Password flow). */
    public void sendPasswordResetOtp(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Password Reset Verification",
                "FORGOT PASSWORD OTP",
                "We received a request to reset your CyberLearnix account password.",
                "Use the OTP below to verify your identity and proceed to set a new password.",
                "5",
                "If you didn't request a password reset, please ignore this email."
        );
    }

    /** Sends an Email Change OTP email. */
    public void sendEmailChangeOtp(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Email Change Verification",
                "CHANGE EMAIL OTP",
                "We received a request to change the email associated with your CyberLearnix account.",
                "Please use the OTP below to verify and confirm this email change.",
                "5",
                "If you didn't request this change, please ignore this email immediately."
        );
    }

    /** Sends a Password Change / Reset OTP email. */
    public void sendPasswordChangeOtp(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Password Change Verification Code",
                "CHANGE PASSWORD OTP<br><span style=\"font-size: 14px; font-weight: normal; color: #a097cc; text-transform: none;\">(WHILE LOGGED IN)</span>",
                "You have requested to change your CyberLearnix account password.",
                "Use the OTP below to confirm this action and update your password.",
                "5",
                "If you didn't request a password change, please secure your account immediately."
        );
    }

    // ─── Notification emails (plain-text, non-OTP) ─────────────────────────────

    public void sendEmailChangeNotification(String toEmail, String subject, String body) {
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
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

    public void sendPasswordChangeNotification(String toEmail, String timestampStr, String ipAddress, String deviceDetails) {
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
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

    // ─── Core: single template sender ──────────────────────────────────────────

    /**
     * Loads universal-otp-email.html, substitutes all placeholders, and sends
     * an HTML email. All public OTP methods delegate here so only one template
     * is ever used regardless of OTP type.
     */
    private void sendOtpWithTemplate(String toEmail,
                                     String otp,
                                     String emailSubject,
                                     String otpTitle,
                                     String reasonDescription,
                                     String instruction,
                                     String expiryMinutes,
                                     String securityWarning) {
        try {
            String firstName = resolveUsernameFromEmail(toEmail);

            String html = getHtmlTemplate()
                    .replace("{{otpReason}}", emailSubject)
                    .replace("{{applicationName}}", "CyberLearnix")
                    .replace("{{otpTitle}}", otpTitle)
                    .replace("{{firstName}}", firstName)
                    .replace("{{reasonDescription}}", reasonDescription)
                    .replace("{{instruction}}", instruction)
                    .replace("{{otp}}", otp)
                    .replace("{{expiryMinutes}}", expiryMinutes)
                    .replace("{{securityWarning}}", securityWarning)
                    .replace("{{secureAccountLink}}", "https://cyberlearnix.com/security")
                    .replace("{{whyReceiveText}}", "These OTPs are sent to verify your identity and protect your account from unauthorized access. Each OTP is valid for a limited time and can be used only once.")
                    .replace("{{year}}", String.valueOf(Year.now().getValue()));

            sendHtmlEmail(toEmail, emailSubject, html);
            log.info("[{}] OTP sent successfully to {}", otpTitle, toEmail);
            if (logOtpValue) {
                log.info("[{}] OTP value for {} is {}", otpTitle, toEmail, otp);
            }
        } catch (Exception e) {
            log.error("[{}] Error sending OTP to {}", otpTitle, toEmail, e);
            throw new RuntimeException("Unable to send OTP email right now. Please try again later.");
        }
    }

    // ─── Infrastructure helpers ─────────────────────────────────────────────────

    private String getHtmlTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/universal-otp-email.html");
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (Exception e) {
            log.error("Failed to load HTML email template", e);
            throw new RuntimeException("Could not load email template");
        }
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}