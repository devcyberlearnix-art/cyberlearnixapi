package com.user.register.security;

import com.cyberlearnix.security.ServiceAuthUtil;
import com.user.register.controller.UserController;
import com.user.register.service.TokenBlacklistService;
import com.user.register.service.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Uses ServiceAuthUtil which was removed - needs refactoring")
@WebMvcTest(UserController.class)
@Import({UnifiedSecurityConfig.class, UserController.class,
    UserDeletionFilterChainTest.FilterTestConfiguration.class})
class UserDeletionFilterChainTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserService userService;
    @Autowired private UnifiedJwtService jwtService;
    @Autowired private ServiceAuthUtil serviceAuthUtil;
    @Autowired private TokenBlacklistService tokenBlacklistService;
    @Autowired private StringRedisTemplate redisTemplate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        clearInvocations(userService);
        Claims claims = mock(Claims.class);
        when(jwtService.validateToken(anyString())).thenReturn(true);
        when(jwtService.isTokenExpired(anyString())).thenReturn(false);
        when(jwtService.extractClaims(anyString())).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");
        when(jwtService.extractUserId("student-token")).thenReturn(UUID.randomUUID().toString());
        when(jwtService.extractRole("student-token")).thenReturn("STUDENT");
        when(jwtService.extractUserId("admin-token")).thenReturn(UUID.randomUUID().toString());
        when(jwtService.extractRole("admin-token")).thenReturn("MAIN_ADMIN");
        when(jwtService.extractAdminType(anyString())).thenReturn("NONE");
        when(jwtService.extractAssignedService(anyString())).thenReturn("NONE");
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);
        when(serviceAuthUtil.getAuthHeaderName()).thenReturn("X-Service-Auth");
        when(serviceAuthUtil.validateServiceToken("service-proof")).thenReturn(true);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(null);
    }

    @Test
    void unauthenticatedDeleteIsRejectedBySecurityFilterChain() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
        verify(userService, never()).deleteUserById(any(UUID.class));
    }

    @Test
    void regularUserCannotDeleteSomeoneElseById() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer student-token")
                        .header("X-Service-Auth", "service-proof"))
                .andExpect(status().isForbidden());
        verify(userService, never()).deleteUserById(any(UUID.class));
    }

    @Test
    void authorizedAdminReachesDeletionService() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer admin-token")
                        .header("X-Service-Auth", "service-proof")
                        .contentType(MediaType.APPLICATION_JSON))
            .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk());
        verify(userService).deleteUserById(any(UUID.class));
    }

    @Configuration
    static class FilterTestConfiguration {
        @Bean UserService userService() { return mock(UserService.class); }
        @Bean UnifiedJwtService unifiedJwtService() { return mock(UnifiedJwtService.class); }
        @Bean ServiceAuthUtil serviceAuthUtil() { return mock(ServiceAuthUtil.class); }
        @Bean TokenBlacklistService tokenBlacklistService() { return mock(TokenBlacklistService.class); }
        @Bean StringRedisTemplate stringRedisTemplate() { return mock(StringRedisTemplate.class); }
        @Bean UnifiedJwtAuthenticationFilter jwtFilter(UnifiedJwtService jwt,
                TokenBlacklistService blacklist, StringRedisTemplate redis) {
            return new UnifiedJwtAuthenticationFilter(jwt, blacklist, redis);
        }
        @Bean UnifiedJwtAuthenticationEntryPoint entryPoint() {
            return mock(UnifiedJwtAuthenticationEntryPoint.class);
        }
        @Bean ServiceAuthFilter serviceAuthFilter(ServiceAuthUtil authUtil) {
            return new ServiceAuthFilter(authUtil);
        }
    }
}
