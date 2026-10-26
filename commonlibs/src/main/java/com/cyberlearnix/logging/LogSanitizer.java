package com.cyberlearnix.logging;

import java.util.regex.Pattern;

public final class LogSanitizer {

    private static final Pattern CRLF_PATTERN = Pattern.compile("[\r\n]");
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(password|passwd|secret|token|otp|authorization|bearer|apikey|access_token|refresh_token)\\s*[:=]\\s*['\"]?([^'\"\\s,;]+)",
            Pattern.CASE_INSENSITIVE
    );

    private LogSanitizer() {
    }

    /**
     * Sanitizes untrusted strings for safe logging by removing CRLF characters,
     * masking common sensitive keywords, and capping max length.
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        // Remove CRLF to prevent log injection
        String sanitized = CRLF_PATTERN.matcher(input).replaceAll(" ");
        // Mask sensitive patterns
        sanitized = SENSITIVE_PATTERN.matcher(sanitized).replaceAll("$1=***MASKED***");
        // Limit length to prevent log flooding
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500) + "...[TRUNCATED]";
        }
        return sanitized;
    }
}
