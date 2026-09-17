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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getCredentials() != null && !auth.getCredentials().toString().isBlank()) {

            String token = auth.getCredentials().toString();

            if (!token.startsWith("Bearer ")) {

                token = "Bearer " + token;

            }

            request.getHeaders().set("Authorization", token);

        }

        return execution.execute(request, body);
    }
}