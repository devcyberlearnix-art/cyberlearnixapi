package com.user.register.util;

import com.user.register.repository.UserRepository;
import com.user.register.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public final class BearerTokenResolver {

    private BearerTokenResolver() {}

    public static UUID resolveUserAccessToken(HttpServletRequest request, JwtUtil jwtUtil, UserRepository userRepository) {
        return resolveUserAccessToken(request.getHeader("Authorization"), jwtUtil, userRepository);
    }

    public static UUID resolveUserAccessToken(String authorizationHeader, JwtUtil jwtUtil, UserRepository userRepository) {
        String token = extractBearerToken(authorizationHeader);
        jwtUtil.requireUserAccessToken(token);
        return jwtUtil.resolveUserIdFromAccessToken(token, userRepository);
    }

    public static String resolveTokenRole(String authorizationHeader, JwtUtil jwtUtil) {
        String token = extractBearerToken(authorizationHeader);
        jwtUtil.requireUserAccessToken(token);
        return jwtUtil.extractRole(token);
    }

    public static UUID resolveUserId(HttpServletRequest request, JwtUtil jwtUtil, UserRepository userRepository) {
        return resolveUserAccessToken(request, jwtUtil, userRepository);
    }

    public static String resolveToken(HttpServletRequest request) {
        return extractBearerToken(request.getHeader("Authorization"));
    }

    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authorization header is required. Send: Authorization: Bearer <user_access_token> "
                            + "(get accessToken from POST /auth/login/password response data.accessToken)");
        }
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token must be in Bearer format: Authorization: Bearer <user_access_token>");
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "User access token is empty. Paste the accessToken from login into: Authorization: Bearer <token>");
        }
        return token;
    }
}
