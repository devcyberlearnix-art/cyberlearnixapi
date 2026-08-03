package com.example.admin.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

public class JwtInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        String url = request.getURI().toString();
        
        // Skip adding auth headers for public endpoints
        if (url.contains("/api/v1/orders") || url.contains("/api/v1/payments") || url.contains("/api/v1/admin/reviews")) {
            return execution.execute(request, body);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getCredentials() != null) {
            String token = auth.getCredentials().toString();
            request.getHeaders().add("Authorization", "Bearer " + token);
        }

        return execution.execute(request, body);
    }
}