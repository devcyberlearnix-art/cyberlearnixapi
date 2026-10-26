package com.user.register.service;

import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class RegistrationOtpVerificationTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private OtpService otpService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserRepository userRepository;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRemoteAddr("127.0.0.1");
        request.setHeader("User-Agent", "Mozilla/5.0 (Test Agent)");
    }

    @Test
    void testValidOtpVerification() {
        // Given
        String email = "validtest@example.com";
        String otp = "123456";
        String otpSessionId = otpService.createSession(email, "registration", otp, 5, 5).sessionId();

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setStatus(User.Status.PENDING_VERIFICATION);
        mockUser.setId(java.util.UUID.randomUUID());
        mockUser.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(mockUser));

        // When
        ResponseEntity<Map<String, Object>> result = registrationService.verifyOTP(
                email, otp, otpSessionId, request, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().containsKey("success"));
        assertTrue((Boolean) result.getBody().get("success"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testInvalidOtp() {
        // Given
        String email = "invalidtest@example.com";
        String correctOtp = "123456";
        String wrongOtp = "654321";
        String otpSessionId = otpService.createSession(email, "registration", correctOtp, 5, 5).sessionId();

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setStatus(User.Status.PENDING_VERIFICATION);
        mockUser.setId(java.util.UUID.randomUUID());
        mockUser.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(mockUser));

        // When
        ResponseEntity<Map<String, Object>> result = registrationService.verifyOTP(
                email, wrongOtp, otpSessionId, request, response);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertFalse((Boolean) result.getBody().get("success"));
        assertTrue(result.getBody().containsKey("remainingAttempts"));
        assertEquals(4, result.getBody().get("remainingAttempts"));
    }

    @Test
    void testExpiredOtp() {
        // Given
        String email = "expiredtest@example.com";
        String otp = "123456";
        String otpSessionId = otpService.createSession(email, "registration", otp, 0, 5).sessionId();

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setStatus(User.Status.PENDING_VERIFICATION);
        mockUser.setId(java.util.UUID.randomUUID());
        mockUser.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(mockUser));

        // When
        ResponseEntity<Map<String, Object>> result = registrationService.verifyOTP(
                email, otp, otpSessionId, request, response);

        // Then
        assertEquals(HttpStatus.GONE, result.getStatusCode());
        assertFalse((Boolean) result.getBody().get("success"));
        assertTrue(result.getBody().get("message").toString().contains("expired"));
    }

    @Test
    void testReusedOtp() {
        // Given
        String email = "reusetest@example.com";
        String otp = "123456";
        String otpSessionId = otpService.createSession(email, "registration", otp, 5, 5).sessionId();

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setStatus(User.Status.PENDING_VERIFICATION);
        mockUser.setId(java.util.UUID.randomUUID());
        mockUser.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(mockUser));

        // First verification - should succeed
        ResponseEntity<Map<String, Object>> firstResult = registrationService.verifyOTP(
                email, otp, otpSessionId, request, response);
        assertEquals(HttpStatus.OK, firstResult.getStatusCode());

        // Second verification with same OTP - should fail
        ResponseEntity<Map<String, Object>> secondResult = registrationService.verifyOTP(
                email, otp, otpSessionId, request, response);

        // Then
        assertEquals(HttpStatus.GONE, secondResult.getStatusCode());
        assertFalse((Boolean) secondResult.getBody().get("success"));
    }

    @Test
    void testUnknownSessionId() {
        // Given
        String email = "unknownsession@example.com";
        String otp = "123456";
        String invalidSessionId = "invalid-session-id-12345";

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setStatus(User.Status.PENDING_VERIFICATION);
        mockUser.setId(java.util.UUID.randomUUID());
        mockUser.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(mockUser));

        // When
        ResponseEntity<Map<String, Object>> result = registrationService.verifyOTP(
                email, otp, invalidSessionId, request, response);

        // Then
        assertEquals(HttpStatus.GONE, result.getStatusCode());
        assertFalse((Boolean) result.getBody().get("success"));
    }

    @Test
    void testTooManyFailedAttempts() {
        // Given
        String email = "locktest@example.com";
        String correctOtp = "123456";
        String wrongOtp = "654321";
        String otpSessionId = otpService.createSession(email, "registration", correctOtp, 5, 5).sessionId();

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setStatus(User.Status.PENDING_VERIFICATION);
        mockUser.setId(java.util.UUID.randomUUID());
        mockUser.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(mockUser));

        // When - 5 failed attempts
        for (int i = 0; i < 5; i++) {
            registrationService.verifyOTP(email, wrongOtp, otpSessionId, request, response);
        }

        // Then - account should be locked
        verify(userRepository, atLeastOnce()).save(argThat(user ->
                user.getStatus() == User.Status.LOCKED
        ));
    }

    @Test
    void testOtpConsumedAfterSuccessfulVerification() {
        // Given
        String email = "consumetest@example.com";
        String otp = "123456";
        String otpSessionId = otpService.createSession(email, "registration", otp, 5, 5).sessionId();

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setStatus(User.Status.PENDING_VERIFICATION);
        mockUser.setId(java.util.UUID.randomUUID());
        mockUser.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(mockUser));

        // When
        ResponseEntity<Map<String, Object>> result = registrationService.verifyOTP(
                email, otp, otpSessionId, request, response);

        // Then - session should be deleted (consumeOnSuccess = true)
        long ttl = otpService.getSessionTtlSeconds(otpSessionId);
        assertEquals(0, ttl, "OTP session should be deleted after successful verification");
    }
}
