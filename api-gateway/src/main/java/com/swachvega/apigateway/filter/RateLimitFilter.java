package com.swachvega.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RateLimitFilter implements GatewayFilter, Ordered {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final int DEFAULT_REQUESTS_PER_MINUTE = 100;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = getClientIp(exchange);
        String key = RATE_LIMIT_PREFIX + clientIp;

        try {
            // Get current count
            Long currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount == null) {
                currentCount = 1L;
                redisTemplate.opsForValue().set(key, 1, Duration.ofMinutes(1));
            } else if (currentCount == 1) {
                // First increment, set expiration
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }

            // Check if limit exceeded
            if (currentCount > DEFAULT_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP: {}, count: {}", clientIp, currentCount);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }

            // Add rate limit headers
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(DEFAULT_REQUESTS_PER_MINUTE));
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(DEFAULT_REQUESTS_PER_MINUTE - currentCount));
            exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));

        } catch (Exception e) {
            log.error("Error checking rate limit for IP: {}", clientIp, e);
            // On error, allow the request (fail open)
        }

        return chain.filter(exchange);
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -1; // High priority to run early
    }
}
