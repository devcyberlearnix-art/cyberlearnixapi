package com.user.register.security;

import com.user.register.entity.User;
import com.user.register.entity.UserSession;
import com.user.register.repository.UserSessionRepository;
import com.user.register.service.TokenBlacklistService;
import com.user.register.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserSessionRepository userSessionRepository;
    private final TokenBlacklistService blacklistService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // get provider name
        OAuth2AuthenticationToken tokenAuth = (OAuth2AuthenticationToken) authentication;
        String provider = tokenAuth.getAuthorizedClientRegistrationId(); // google/github/linkedin

        // get email based on provider
        String email = oAuth2User.getAttribute("email");

        // GitHub fallback: users with private email have null email attribute
        // Use login username as identifier (stored separately, not as fake email)
        if (email == null && "github".equals(provider)) {
            String login = oAuth2User.getAttribute("login");
            // Note: to get real GitHub email, you need 'user:email' scope + GitHub Emails API call
            // For now, use login as a unique identifier stored in provider field
            email = login + "@github-user.local";
        }

        // LinkedIn fallback (OpenID Connect)
        if (email == null && "linkedin".equals(provider)) {
            email = oAuth2User.getAttribute("email"); // provided via openid scope
        }

        if (email == null) {
            throw new RuntimeException("Unable to retrieve email from provider: " + provider);
        }

        // save or login user
        User user = userService.socialLogin(email, provider);

        // generate JWT
        String token = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getRole().name()
        );

        // Invalidate old sessions
        List<UserSession> existingSessions = userSessionRepository.findByUser(user);
        for (UserSession s : existingSessions) {
            if (s.getAccessToken() != null) {
                blacklistService.blacklistToken(s.getAccessToken());
            }
            if (s.getRefreshToken() != null) {
                blacklistService.blacklistToken(s.getRefreshToken());
            }
        }
        userSessionRepository.deleteAll(existingSessions);

        // Save new session
        String deviceInfo = request.getHeader("User-Agent");
        if (deviceInfo == null) deviceInfo = "Unknown Device";
        String ipAddress = request.getRemoteAddr();

        UserSession userSession = UserSession.builder()
                .user(user)
                .accessToken(token)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        userSessionRepository.save(userSession);

        // response JSON
        response.setContentType("application/json");
        response.getWriter().write(
                "{ \"email\": \"" + email + "\", \"token\": \"" + token + "\", \"provider\": \"" + provider + "\" }"
        );
    }
}