package com.cyberlearnix.commonlibs.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SimpleErrorResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private int status;
    private String traceId;
}
