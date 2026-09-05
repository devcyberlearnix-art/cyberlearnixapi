package com.example.admin.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
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
import com.example.admin.entity.Admin;
import com.example.admin.repository.AdminRepository;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean logOtpValue;
    private final AdminRepository adminRepository;

    @Value("${spring.mail.username:noreply@cyberlearnix.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.otp.log-value:true}") boolean logOtpValue,
                        AdminRepository adminRepository) {
        this.mailSender = mailSender;
        this.logOtpValue = logOtpValue;
        this.adminRepository = adminRepository;
    }

    private String resolveUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            String username = email.substring(0, email.indexOf('@')).trim();
            if (!username.isEmpty()) {
                return username;
            }
        }
        return "Admin";
    }

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

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ─── Public OTP email methods ───────────────────────────────────────────────

    /** Sends a Login Verification OTP email to an admin. */
    public void sendOtp(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Admin Login Verification",
                "ADMIN LOGIN OTP",
                "A login attempt was made to your Admin account on CyberLearnix.",
                "For security reasons, please verify your identity using the OTP below.",
                "5",
                "If you didn't attempt to log in, please contact your system administrator immediately."
        );
    }

    /** Sends a Password Reset OTP email (Forgot Password flow) to an admin. */
    public void sendPasswordResetOtp(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Admin Password Reset Verification",
                "FORGOT PASSWORD OTP",
                "We received a request to reset your CyberLearnix Admin account password.",
                "Use the OTP below to verify your identity and proceed to set a new password.",
                "5",
                "If you didn't request a password reset, please contact your system administrator immediately."
        );
    }

    /** Sends an Email Change OTP email to an admin. */
    public void sendEmailChangeOtp(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Email Change Verification",
                "CHANGE EMAIL OTP",
                "We received a request to change the email associated with your CyberLearnix Admin account.",
                "Please use the OTP below to verify and confirm this email change.",
                "5",
                "If you didn't request this change, please ignore this email immediately."
        );
    }

    public void sendEmailChangeNotification(String toEmail, String subject, String body) {
        // Since the template is strictly OTP formatted, we either create a new template or reuse a simpler plain text. 
        // We will keep plain text for notifications if it doesn't fit the OTP template, but to be safe and use HTML, 
        // we can just send it as plain text as requested by the original code signature, 
        // or wrap it in a simple HTML body. We will stick to plain text for this non-OTP method.
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[EmailChange] Notification email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("[EmailChange] Could not send notification to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /** Sends a Password Change OTP email to an admin. */
    public void sendPasswordChangeOtp(String toEmail, String otp) {
        sendOtpWithTemplate(
                toEmail,
                otp,
                "CyberLearnix — Password Change Verification",
                "CHANGE PASSWORD OTP<br><span style=\"font-size: 14px; font-weight: normal; color: #a097cc; text-transform: none;\">(WHILE LOGGED IN)</span>",
                "You have requested to change your CyberLearnix Admin account password.",
                "Use the OTP below to confirm this action and update your password.",
                "5",
                "If you didn't request a password change, please secure your account immediately."
        );
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
                    .replace("{{secureAccountLink}}", "https://cyberlearnix.com/admin/security")
                    .replace("{{whyReceiveText}}", "These OTPs are sent to verify your identity and protect your account from unauthorized access. Each OTP is valid for a limited time and can be used only once.")
                    .replace("{{year}}", String.valueOf(Year.now().getValue()));

            sendHtmlEmail(toEmail, emailSubject, html);
            log.info("[{}] OTP sent successfully to {}", otpTitle, toEmail);
            if (logOtpValue) {
                log.info("[{}] OTP value for {} is {}", otpTitle, toEmail, otp);
            }
        } catch (RuntimeException e) {
            log.error("[{}] Failed to send OTP to {}: {}", otpTitle, toEmail, e.getMessage());
            throw e;
        }
    }

    public void sendPasswordChangeNotification(String toEmail, String timestampStr, String ipAddress, String deviceDetails) {
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom(fromEmail);
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
            mailSender.send(message);
            log.info("Password change security notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password change notification to {}: {}", toEmail, e.getMessage());
        }
    }
}