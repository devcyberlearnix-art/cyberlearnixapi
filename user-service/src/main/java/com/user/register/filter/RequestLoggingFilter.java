package com.user.register.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.err.println("=== REQUEST LOGGING FILTER - HIGHEST PRECEDENCE ===");
        System.err.println("URI: " + request.getRequestURI());
        System.err.println("Method: " + request.getMethod());
        System.err.println("Content-Type: " + request.getContentType());
        System.err.println("===============================================");
        filterChain.doFilter(request, response);
    }
}
