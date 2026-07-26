package com.example.admin.rateLimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LoginRateLimiter {

    private final Bucket bucket;

    public LoginRateLimiter() {

        Bandwidth limit = Bandwidth.simple(5, Duration.ofMinutes(1));
        bucket = Bucket.builder().addLimit(limit).build();
    }

    public void checkLimit() {

        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("Too many login attempts. Try again later.");
        }
    }
}
