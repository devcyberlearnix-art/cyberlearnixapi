package com.cyberlearnix.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public final class ApiSecurityErrorWriter {

    private ApiSecurityErrorWriter() {
    }

    public static void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String code,
            String message) throws IOException {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        response.setStatus(status);
        response.setContentType("application/json");
        response.setHeader("X-Trace-Id", traceId);
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"code\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"traceId\":\"%s\"}",
                Instant.now(), status, code, message, request.getRequestURI(), traceId));
    }
}