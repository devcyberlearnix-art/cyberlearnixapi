package com.user.register.security;

import com.user.register.entity.User;
import com.user.register.entity.UserSession;
import com.user.register.repository.UserSessionRepository;
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

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserSessionRepository userSessionRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken tokenAuth = (OAuth2AuthenticationToken) authentication;
        String provider = tokenAuth.getAuthorizedClientRegistrationId();

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = oAuth2User.getAttribute("login") + "@github.com";
        }

        // 1. Create or retrieve user
        User user = userService.socialLogin(email, provider);

        // 2. Generate JWT Access Token
        String token = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getRole().name()
        );

        // 3. Persist session to Database (Crucial for the Filter to pass)
        UserSession session = new UserSession();
        session.setUser(user);
        session.setAccessToken(token);
        session.setCreatedAt(LocalDateTime.now());
        // Setting a 7-day expiry as a best practice
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setDeviceInfo(request.getHeader("User-Agent"));
        session.setIpAddress(request.getRemoteAddr());
        userSessionRepository.save(session);

        // 4. Return JSON response with the token
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format("{ \"email\": \"%s\", \"token\": \"%s\", \"provider\": \"%s\" }",
                        email, token, provider)
        );
    }
}