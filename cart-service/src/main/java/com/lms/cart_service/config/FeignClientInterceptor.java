package com.lms.cart_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && !authHeader.isBlank()) {
                template.header("Authorization", authHeader);
            }

            String traceId = (String) request.getAttribute("X-Trace-Id");
            if (traceId == null || traceId.isBlank()) {
                traceId = request.getHeader("X-Trace-Id");
            }
            if (traceId != null && !traceId.isBlank()) {
                template.header("X-Trace-Id", traceId);
            }

            String userId = request.getHeader("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                template.header("X-User-Id", userId);
            }

            String userRole = request.getHeader("X-User-Role");
            if (userRole != null && !userRole.isBlank()) {
                template.header("X-User-Role", userRole);
            }
        }
    }
}
