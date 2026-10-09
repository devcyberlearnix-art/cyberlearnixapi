package com.lms.review.client;

import com.lms.review.config.FeignConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service-client",
        url = "${user.service.url}",
        fallback = UserClientFallback.class,
        configuration = FeignConfig.class
)
@ConditionalOnProperty(name = "user.service.enabled", havingValue = "true", matchIfMissing = true)
public interface UserClient {

    @GetMapping("/api/v1/users/{userId}")
    UserApiResponse getUserById(@PathVariable("userId") UUID userId);
}
