package com.cyberlearnix.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldSerializeStandardErrorResponse() throws Exception {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put("email", "must be a well-formed email address");
        fieldErrors.put("password", "must not be blank");

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.parse("2026-10-16T12:00:00Z"),
                400,
                "VALIDATION_ERROR",
                "Validation failed for request",
                "/api/v1/auth/register",
                "test-trace-id-12345",
                fieldErrors
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"status\":400");
        assertThat(json).contains("\"code\":\"VALIDATION_ERROR\"");
        assertThat(json).contains("\"message\":\"Validation failed for request\"");
        assertThat(json).contains("\"path\":\"/api/v1/auth/register\"");
        assertThat(json).contains("\"traceId\":\"test-trace-id-12345\"");
        assertThat(json).contains("\"fieldErrors\":{\"email\":\"must be a well-formed email address\",\"password\":\"must not be blank\"}");
    }

    @Test
    void shouldOmitFieldErrorsWhenNull() throws Exception {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.parse("2026-10-16T12:00:00Z"),
                404,
                "RESOURCE_NOT_FOUND",
                "Course not found with id: 999",
                "/api/v1/courses/999",
                "trace-abc"
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).doesNotContain("\"fieldErrors\"");
        assertThat(json).contains("\"status\":404");
        assertThat(json).contains("\"code\":\"RESOURCE_NOT_FOUND\"");
    }
}
