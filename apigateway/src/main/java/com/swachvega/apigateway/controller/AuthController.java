package com.swachvega.apigateway.controller;

import com.swachvega.apigateway.model.*;
import com.swachvega.apigateway.security.SimpleJwtTokenProvider;
import com.swachvega.commonlibs.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/consumer/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

        private final SimpleJwtTokenProvider jwtTokenProvider;
        private final WebClient.Builder webClientBuilder;

        @Value("${userservice.url:http://swachvega-userservice:8080}")
        private String userServiceUrl;

        /**
         * Step 1: Send OTP to phone number (unified entry point as per PRD)
         */
        @PostMapping("/send-otp")
        public Mono<ResponseEntity<OtpResponseDTO>> sendOtp(@RequestBody PhoneOtpRequestDTO request) {
                log.info("OTP request for phone: {}", request.getPhone());

                return webClientBuilder.build()
                                .post()
                                .uri(userServiceUrl + "/api/consumer/auth/send-otp")
                                .bodyValue(Map.of(
                                                "phone", request.getPhone(),
                                                "deliveryMethod",
                                                request.getDeliveryMethod() != null ? request.getDeliveryMethod()
                                                                : "SMS"))
                                .retrieve()
                                .bodyToMono(OtpResponseDTO.class)
                                .map(response -> ResponseEntity.ok(OtpResponseDTO.builder()
                                                .success(response.isSuccess())
                                                .message(response.getMessage())
                                                .otpSessionId(response.getOtpSessionId())
                                                .expirySeconds(response.getExpirySeconds() != null
                                                                ? response.getExpirySeconds()
                                                                : 0)
                                                .deliveryMethod(response.getDeliveryMethod())
                                                .maskedContact(response.getMaskedContact())
                                                .resendCooldownSeconds(response.getResendCooldownSeconds() != null
                                                                ? response.getResendCooldownSeconds()
                                                                : 0)
                                                .build()))
                                .onErrorResume(Exception.class, ex -> {
                                        log.error("OTP request failed: {}", ex.getMessage());
                                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                        .body(OtpResponseDTO.builder()
                                                                        .success(false)
                                                                        .message("Failed to send OTP. Please try again.")
                                                                        .build()));
                                });
        }

        /**
         * Step 2: Verify OTP and determine flow (existing user login vs new user
         * registration)
         */
        @PostMapping("/verify-otp")
        public Mono<ResponseEntity<PhoneOtpValidationResponseDTO>> verifyOtp(
                        @RequestBody PhoneOtpValidationDTO request) {
                log.info("OTP verification for phone: {}", request.getPhone());

                return webClientBuilder.build()
                                .post()
                                .uri(userServiceUrl + "/api/consumer/auth/verify-otp")
                                .bodyValue(Map.of(
                                                "phone", request.getPhone(),
                                                "otp", request.getOtp(),
                                                "otpSessionId", request.getOtpSessionId()))
                                .retrieve()
                                .bodyToMono(PhoneOtpValidationResponseDTO.class)
                                .map(response -> {
                                        if (!response.isSuccess()) {
                                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                                .body(PhoneOtpValidationResponseDTO.builder()
                                                                                .success(false)
                                                                                .message(response.getMessage())
                                                                                .userExists(false)
                                                                                .build());
                                        }

                                        if (response.isUserExists()) {
                                                // Existing user - generate JWT tokens and complete login
                                                String sessionId = UUID.randomUUID().toString();

                                                // Create additional claims for enhanced token
                                                Map<String, Object> additionalClaims = new HashMap<>();
                                                additionalClaims.put("loginType", "phone_otp");
                                                additionalClaims.put("isPhoneVerified", true);

                                                String accessToken = jwtTokenProvider.generateAccessToken(
                                                                response.getUser().getUserId(),
                                                                response.getUser().getUsername(),
                                                                sessionId,
                                                                response.getUser().getRole(),
                                                                response.getUser().getEmail(),
                                                                response.getUser().getFirstName() + " "
                                                                                + response.getUser().getLastName(), // Combine
                                                                                                                    // first
                                                                                                                    // and
                                                                                                                    // last
                                                                                                                    // name
                                                                response.getUser().getPhoneNumber(),
                                                                additionalClaims);

                                                String refreshToken = jwtTokenProvider.generateRefreshToken(
                                                                response.getUser().getUserId(),
                                                                sessionId);

                                                return ResponseEntity.ok(PhoneOtpValidationResponseDTO.builder()
                                                                .success(true)
                                                                .message("Login successful")
                                                                .userExists(true)
                                                                .user(response.getUser())
                                                                .accessToken(accessToken)
                                                                .refreshToken(refreshToken)
                                                                .expiresIn(jwtTokenProvider
                                                                                .getAccessTokenExpirationSeconds())
                                                                .build());
                                        } else {
                                                // New user - return temp token for registration
                                                return ResponseEntity.ok(PhoneOtpValidationResponseDTO.builder()
                                                                .success(true)
                                                                .message("OTP verified. Please complete your registration.")
                                                                .userExists(false)
                                                                .tempToken(response.getTempToken())
                                                                .build());
                                        }
                                })
                                .onErrorResume(WebClientResponseException.class, ex -> {
                                        log.error("OTP verification failed: {}", ex.getMessage());
                                        return Mono.just(ResponseEntity.status(ex.getStatusCode())
                                                        .body(PhoneOtpValidationResponseDTO.builder()
                                                                        .success(false)
                                                                        .message("OTP verification failed")
                                                                        .userExists(false)
                                                                        .build()));
                                });
        }

        /**
         * Step 3: Complete registration for new users using temp token
         */
        @PostMapping("/register")
        public Mono<ResponseEntity<UserRegistrationResponseDTO>> completeRegistration(
                        @RequestBody UserRegistrationCompleteDTO request) {
                log.info("Registration completion request");

                // Build request body with null handling for optional fields
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("tempToken", request.getTempToken());
                requestBody.put("name", request.getName());
                requestBody.put("email", request.getEmail());
                if (request.getAlternatePhone() != null) {
                        requestBody.put("alternatePhone", request.getAlternatePhone());
                }

                return webClientBuilder.build()
                                .post()
                                .uri(userServiceUrl + "/api/consumer/auth/register")
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(UserRegistrationResponseDTO.class)
                                .map(response -> {
                                        if (!response.isSuccess()) {
                                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                                .body(UserRegistrationResponseDTO.builder()
                                                                                .success(false)
                                                                                .message(response.getMessage())
                                                                                .build());
                                        }

                                        // Generate JWT tokens for the new user
                                        String sessionId = UUID.randomUUID().toString();

                                        // Create additional claims for enhanced token
                                        Map<String, Object> additionalClaims = new HashMap<>();
                                        additionalClaims.put("loginType", "phone_registration");
                                        additionalClaims.put("isNewUser", true);
                                        additionalClaims.put("isPhoneVerified", true);
                                        additionalClaims.put("registrationCompleted", true);

                                        String accessToken = jwtTokenProvider.generateAccessToken(
                                                        response.getUser().getUserId(),
                                                        response.getUser().getUsername(),
                                                        sessionId,
                                                        response.getUser().getRole(),
                                                        response.getUser().getEmail(),
                                                        response.getUser().getFirstName() + " "
                                                                        + response.getUser().getLastName(), // Combine
                                                                                                            // first and
                                                                                                            // last name
                                                        response.getUser().getPhoneNumber(),
                                                        additionalClaims);

                                        String refreshToken = jwtTokenProvider.generateRefreshToken(
                                                        response.getUser().getUserId(),
                                                        sessionId);

                                        UserRegistrationResponseDTO registrationResponse = UserRegistrationResponseDTO
                                                        .builder()
                                                        .success(true)
                                                        .message("Registration completed successfully")
                                                        .accessToken(accessToken)
                                                        .refreshToken(refreshToken)
                                                        .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                                        .user(response.getUser())
                                                        .build();

                                        log.info("Registration completed successfully");
                                        return ResponseEntity.ok(registrationResponse);
                                })
                                .onErrorResume(WebClientResponseException.class, ex -> {
                                        log.error("Registration completion failed: {}", ex.getMessage());
                                        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                                .body(UserRegistrationResponseDTO.builder()
                                                                                .success(false)
                                                                                .message("Invalid or expired temporary token")
                                                                                .build()));
                                        } else if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                                                return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                                                                .body(UserRegistrationResponseDTO.builder()
                                                                                .success(false)
                                                                                .message("Email already registered")
                                                                                .build()));
                                        }
                                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                        .body(UserRegistrationResponseDTO.builder()
                                                                        .success(false)
                                                                        .message("Registration failed")
                                                                        .build()));
                                });
        }

        /**
         * Refresh token endpoint
         */
        @PostMapping("/refresh")
        public Mono<ResponseEntity<AuthResponse>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
                String token = extractToken(refreshToken);

                return jwtTokenProvider.validateRefreshToken(token)
                                .flatMap(claims -> {
                                        String userId = (String) claims.get("sub");
                                        String sessionId = (String) claims.get("sessionId");

                                        // In production, you'd fetch user details from database/cache
                                        // For now, using basic info to generate new access token
                                        Map<String, Object> additionalClaims = new HashMap<>();
                                        additionalClaims.put("tokenRefreshed", true);
                                        additionalClaims.put("refreshedAt", System.currentTimeMillis());

                                        // Generate new access token with enhanced information
                                        String newAccessToken = jwtTokenProvider.generateAccessToken(
                                                        userId,
                                                        "user", // You'd typically get this from session storage or user
                                                                // service
                                                        sessionId,
                                                        "CONSUMER", // Default role, should be fetched from user service
                                                        null, // email - should be fetched from user service
                                                        null, // fullName - should be fetched from user service
                                                        null, // phoneNumber - should be fetched from user service
                                                        additionalClaims);

                                        AuthResponse response = AuthResponse.builder()
                                                        .success(true)
                                                        .message("Token refreshed")
                                                        .accessToken(newAccessToken)
                                                        .refreshToken(refreshToken) // Keep same refresh token
                                                        .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                                        .tokenType("Bearer")
                                                        .sessionId(sessionId)
                                                        .build();

                                        return Mono.just(ResponseEntity.ok(response));
                                })
                                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(AuthResponse.builder()
                                                                .success(false)
                                                                .message("Invalid refresh token")
                                                                .build())));
        }

        /**
         * Health check endpoint
         */
        @GetMapping("/health")
        public Mono<ResponseEntity<Map<String, String>>> health() {
                return Mono.just(ResponseEntity.ok(Map.of(
                                "status", "healthy",
                                "service", "auth-gateway",
                                "timestamp", String.valueOf(System.currentTimeMillis()))));
        }

        /**
         * Test endpoint to verify JWT validation works
         */
        @GetMapping("/profile")
        public Mono<ResponseEntity<Map<String, String>>> getProfile(
                        @RequestHeader("X-User-Id") String userId,
                        @RequestHeader("X-Username") String username,
                        @RequestHeader("X-User-Role") String role) {

                return Mono.just(ResponseEntity.ok(Map.of(
                                "userId", userId,
                                "username", username,
                                "role", role,
                                "message", "JWT validation successful")));
        }

        private String extractToken(String authHeader) {
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        return authHeader.substring(7);
                }
                return authHeader;
        }
}
