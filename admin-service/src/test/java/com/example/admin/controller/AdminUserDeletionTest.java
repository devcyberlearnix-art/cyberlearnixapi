package com.example.admin.controller;

import com.cyberlearnix.security.ServiceAuthUtil;
import com.example.admin.dto.AdminDeleteUserResponse;
import com.example.admin.repository.AdminRepository;
import com.example.admin.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserDeletionTest {

    private final AdminUserService userService = mock(AdminUserService.class);
    private final AdminRepository adminRepository = mock(AdminRepository.class);
    private final ServiceAuthUtil serviceAuthUtil = mock(ServiceAuthUtil.class);
    private final AdminUserController controller = new AdminUserController(
            userService, adminRepository, serviceAuthUtil);

    @Test
    void missingUserRemainsNotFound() {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var response = controller.deleteUser(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void downstreamFailureRemainsServiceUnavailable() {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenThrow(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User service is unavailable"));

        var response = controller.deleteUser(id);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User service is unavailable", response.getBody().getMessage());
    }

    @Test
    void invalidInternalServiceProofIsRejected() {
        when(serviceAuthUtil.validateServiceToken("bad-proof")).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Service-Auth", "bad-proof");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> controller.getRefreshEligibility(UUID.randomUUID(), request));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(adminRepository);
    }
}
