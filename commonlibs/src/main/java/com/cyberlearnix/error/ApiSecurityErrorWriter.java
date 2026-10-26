package com.cyberlearnix.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public final class ApiSecurityErrorWriter {

    private static final Logger log = LoggerFactory.getLogger(ApiSecurityErrorWriter.class);

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

        log.warn("[traceId={}] Security rejection: status={} code={} method={} path={} message={}",
                traceId, status, code, request.getMethod(), request.getRequestURI(), message);

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Trace-Id", traceId);

        String safeMsg = message == null ? "" : message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
        String safePath = request.getRequestURI() == null ? "" : request.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"");

        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"code\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"traceId\":\"%s\"}",
                Instant.now().toString(), status, code, safeMsg, safePath, traceId);
        response.getWriter().write(json);
    }
}
