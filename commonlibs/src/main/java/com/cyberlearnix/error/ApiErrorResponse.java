package com.cyberlearnix.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String traceId,
        Map<String, String> fieldErrors) {

    public ApiErrorResponse(Instant timestamp, int status, String code, String message, String path, String traceId) {
        this(timestamp, status, code, message, path, traceId, null);
    }
}
