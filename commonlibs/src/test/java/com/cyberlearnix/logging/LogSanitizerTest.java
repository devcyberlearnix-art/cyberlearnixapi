package com.cyberlearnix.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogSanitizerTest {

    @Test
    void shouldSanitizeCrlfInjection() {
        String input = "user_input\r\nINFO 2026-10-16 Malicious Log Injection\nAnother line";
        String sanitized = LogSanitizer.sanitize(input);

        assertThat(sanitized).doesNotContain("\r");
        assertThat(sanitized).doesNotContain("\n");
        assertThat(sanitized).isEqualTo("user_input  INFO 2026-10-16 Malicious Log Injection Another line");
    }

    @Test
    void shouldMaskSensitiveFields() {
        String input = "login with password=mySecretPassword123 and token=eyJhbGciOiJIUzI1NiJ9.test and secret: topSecretValue";
        String sanitized = LogSanitizer.sanitize(input);

        assertThat(sanitized).doesNotContain("mySecretPassword123");
        assertThat(sanitized).doesNotContain("topSecretValue");
        assertThat(sanitized).contains("password=***MASKED***");
    }

    @Test
    void shouldHandleNullAndEmpty() {
        assertThat(LogSanitizer.sanitize(null)).isEqualTo("");
        assertThat(LogSanitizer.sanitize("")).isEqualTo("");
        assertThat(LogSanitizer.sanitize("   ")).isEqualTo("   ");
    }
}
