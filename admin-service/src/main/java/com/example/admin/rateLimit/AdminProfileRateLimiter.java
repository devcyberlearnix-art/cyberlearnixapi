package com.example.admin.rateLimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdminProfileRateLimiter {

    private final Map<UUID, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.simple(5, Duration.ofMinutes(1));
        return Bucket.builder().addLimit(limit).build();
    }

    public void checkLimit(UUID adminId) {
        if (adminId == null) {
            return;
        }
        Bucket bucket = cache.computeIfAbsent(adminId, k -> createNewBucket());
        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Profile update rate limit exceeded (Max 5 updates per minute). Please try again later."
            );
        }
    }
}
