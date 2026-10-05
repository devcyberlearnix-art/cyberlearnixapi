package com.user.register.security;

import com.cyberlearnix.commonlibs.dto.UserLoginEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.register.dto.unified.LoginResponse;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.service.SessionService;
import com.user.register.util.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UnifiedJwtService unifiedJwtService;
    private final SessionService sessionService;
    private final KafkaTemplate<String, UserLoginEvent> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${app.kafka.topic.user-login:user-login-topic}")
    private String userLoginTopic;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken tokenAuth = (OAuth2AuthenticationToken) authentication;
        String provider = tokenAuth.getAuthorizedClientRegistrationId().toLowerCase();

        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getAttribute("sub") != null
                ? String.valueOf(oAuth2User.getAttribute("sub"))
                : (oAuth2User.getAttribute("id") != null ? String.valueOf(oAuth2User.getAttribute("id")) : null);

        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String profilePhoto = oAuth2User.getAttribute("picture");

        if ("github".equals(provider)) {
            String login = oAuth2User.getAttribute("login");
            if (email == null) {
                email = (login != null ? login : (providerId != null ? providerId : "user")) + "@github-user.local";
            }
            if (profilePhoto == null) {
                profilePhoto = oAuth2User.getAttribute("avatar_url");
            }
            if (firstName == null && oAuth2User.getAttribute("name") != null) {
                String fullName = oAuth2User.getAttribute("name");
                String[] parts = fullName.split(" ", 2);
                firstName = parts[0];
                if (parts.length > 1) {
                    lastName = parts[1];
                }
            }
        }

        if (email == null && "linkedin".equals(provider)) {
            email = oAuth2User.getAttribute("email");
        }

        if (email == null) {
            throw new RuntimeException("Unable to retrieve email from provider: " + provider);
        }

        email = email.trim().toLowerCase();

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
            if (user.getStatus() == User.Status.SUSPENDED || user.getStatus() == User.Status.PENDING_VERIFICATION) {
                user.setStatus(User.Status.ACTIVE);
            }
            if (user.getProvider() == null || user.getProvider().isBlank()) {
                user.setProvider(provider);
            }
            if (providerId != null && (user.getProviderId() == null || user.getProviderId().isBlank())) {
                user.setProviderId(providerId);
            }
            if (profilePhoto != null && (user.getProfilePhoto() == null || user.getProfilePhoto().isBlank())) {
                user.setProfilePhoto(profilePhoto);
            }
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLogin(LocalDateTime.now());
            user = userRepository.save(user);
        } else {
            user = User.builder()
                    .email(email)
                    .role(User.Role.STUDENT)
                    .status(User.Status.ACTIVE)
                    .provider(provider)
                    .providerId(providerId)
                    .profilePhoto(profilePhoto)
                    .firstName(firstName != null ? encryptField(firstName) : "")
                    .lastName(lastName != null ? encryptField(lastName) : "")
                    .createdAt(LocalDateTime.now())
                    .lastLoginAt(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
        }

        // Generate JWT tokens
        String accessToken = unifiedJwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                null,
                null
        );

        String refreshToken = unifiedJwtService.generateRefreshToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name()
        );

        // Save session
        sessionService.createSession(user, request, accessToken, refreshToken);

        // Build unified response
        LoginResponse.UserData userData = LoginResponse.UserData.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(decryptField(user.getFirstName()))
                .lastName(decryptField(user.getLastName()))
                .mobileNumber(decryptField(user.getMobile()))
                .role(user.getRole().name())
                .adminType("NONE")
                .assignedService("NONE")
                .permissions(List.of("READ_COURSES", "ENROLL_COURSE", "VIEW_PROFILE", "UPDATE_PROFILE"))
                .verified(true)
                .approved(true)
                .build();

        LoginResponse.AuthenticationInfo authInfo = LoginResponse.AuthenticationInfo.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn("15 minutes")
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn("30 days")
                .build();

        LoginResponse.SessionInfo sessionInfo = LoginResponse.SessionInfo.builder()
                .loginTime(LocalDateTime.now().toString())
                .ipAddress(request.getRemoteAddr())
                .device(request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "Unknown Device")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .success(true)
                .message("OAuth2 authentication successful with " + provider)
                .user(userData)
                .authentication(authInfo)
                .sessionInfo(sessionInfo)
                .timestamp(LocalDateTime.now())
                .build();

        // Publish Kafka event
        try {
            String fName = decryptField(user.getFirstName());
            String lName = decryptField(user.getLastName());
            String fullName = (fName != null ? fName : "") + " " + (lName != null ? lName : "");
            UserLoginEvent event = new UserLoginEvent(
                    UUID.randomUUID(),
                    "USER_LOGIN",
                    user.getId(),
                    user.getEmail(),
                    fullName.trim(),
                    LocalDateTime.now(),
                    user.getIpAddress(),
                    user.getDevice(),
                    user.getBrowser(),
                    user.getOs(),
                    provider.toUpperCase(),
                    false
            );
            kafkaTemplate.send(userLoginTopic, user.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish Kafka login event for user {}", user.getId(), e);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
    }

    private String encryptField(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            return SecurityUtils.encrypt(value, "1234567890123456");
        } catch (Exception e) {
            return value;
        }
    }

    private String decryptField(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) return "";
        try {
            return SecurityUtils.decrypt(encrypted, "1234567890123456");
        } catch (Exception e) {
            return encrypted;
        }
    }
}
