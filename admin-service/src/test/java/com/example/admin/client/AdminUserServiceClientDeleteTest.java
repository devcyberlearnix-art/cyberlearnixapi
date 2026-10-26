package com.example.admin.client;

import com.cyberlearnix.security.ServiceAuthUtil;
import com.example.admin.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminUserServiceClientDeleteTest {

    private final RestTemplate rest = mock(RestTemplate.class);
    private final JwtService jwt = mock(JwtService.class);
    private final ServiceAuthUtil serviceAuth = mock(ServiceAuthUtil.class);
    private AdminUserServiceClient client;

    @BeforeEach
    void setUp() {
        client = new AdminUserServiceClient(rest, jwt, serviceAuth);
        ReflectionTestUtils.setField(client, "userServiceUrl", "http://user-service");
        when(jwt.createServiceAuthHeaders()).thenReturn(new HttpHeaders());
        when(serviceAuth.getAuthHeaderName()).thenReturn("X-Service-Auth");
        when(serviceAuth.generateServiceToken()).thenReturn("signed-service-proof");
    }

    @Test
    void userServiceNotFoundRemains404() {
        when(rest.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        HttpHeaders.EMPTY, "user missing".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> client.deleteUser(UUID.randomUUID()));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void userServiceNetworkFailureBecomes503() {
        when(rest.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> client.deleteUser(UUID.randomUUID()));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }
}
