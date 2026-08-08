package com.example.admin.security;



import com.example.admin.entity.AssignedService;

import jakarta.servlet.*;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;



import java.io.IOException;

import java.util.List;

import java.util.UUID;



@Component

@RequiredArgsConstructor

public class JwtAuthFilter implements Filter {



    private final JwtService jwtService;



    @Override

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)

            throws IOException, ServletException {



        HttpServletRequest req = (HttpServletRequest) request;

        HttpServletResponse res = (HttpServletResponse) response;



        String requestURI = req.getRequestURI();

        String method = req.getMethod();

        // Skip authentication for permitAll endpoints

        boolean isPermitAll = isPermitAllEndpoint(requestURI, method);

        if (!isPermitAll) {

            authenticateBearer(req);

        }



        chain.doFilter(request, response);

    }



    private boolean isPermitAllEndpoint(String requestURI, String method) {

        // Course endpoints (public/internal)

        if (requestURI.equals("/api/v1/admin/courses") && "GET".equalsIgnoreCase(method)) {

            return true;

        }



        if (requestURI.matches("/api/v1/admin/courses/\\d+") && "GET".equalsIgnoreCase(method)) {

            return true;

        }



        if (requestURI.matches("/api/v1/admin/courses/\\d+/approve") && "PUT".equalsIgnoreCase(method)) {

            return true;

        }



        if (requestURI.matches("/api/v1/admin/courses/\\d+/reject") && "PUT".equalsIgnoreCase(method)) {

            return true;

        }



        if (requestURI.matches("/api/v1/admin/courses/\\d+") && "DELETE".equalsIgnoreCase(method)) {

            return true;

        }



        if (requestURI.matches("/api/v1/admin/instructors/\\d+/courses") && "GET".equalsIgnoreCase(method)) {

            return true;

        }



        // Section & Lecture content endpoints (allow internal service calls without

        // admin JWT)

        if (requestURI.matches("/api/v1/admin/courses/\\d+/sections") && "POST".equalsIgnoreCase(method)) {

            return true;

        }

        if (requestURI.matches("/api/v1/admin/sections/\\d+/lectures") && "POST".equalsIgnoreCase(method)) {

            return true;

        }

        if (requestURI.matches("/api/v1/admin/sections/\\d+/lectures/\\d+/approve") && "PUT".equalsIgnoreCase(method)) {

            return true;

        }

        if (requestURI.matches("/api/v1/admin/sections/\\d+/lectures/\\d+/reject") && "PUT".equalsIgnoreCase(method)) {

            return true;

        }

        if (requestURI.matches("/api/v1/admin/sections/\\d+") && "DELETE".equalsIgnoreCase(method)) {

            return true;

        }

        if (requestURI.matches("/api/v1/admin/sections/\\d+/lectures/\\d+") && "DELETE".equalsIgnoreCase(method)) {

            return true;

        }



        // Admin API endpoints for orders, payments, and reviews

        if (requestURI.equals("/api/v1/admin/orders") && "GET".equalsIgnoreCase(method)) {

            return true;

        }

        if (requestURI.equals("/api/v1/admin/payments") && "GET".equalsIgnoreCase(method)) {

            return true;

        }

        if (requestURI.equals("/api/v1/admin/reviews") && "GET".equalsIgnoreCase(method)) {

            return true;

        }



        return false;

    }



    private boolean authenticateBearer(HttpServletRequest req) {

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {

            authHeader = req.getHeader("authorization");

        }

        if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {

            SecurityContextHolder.clearContext();

            return false;

        }



        String token = authHeader.substring(7).trim();

        if (token.isBlank()) {

            SecurityContextHolder.clearContext();

            return false;

        }



        try {

            UUID adminId = jwtService.extractAdminId(token);

            String role = jwtService.extractRole(token);

            String adminType = jwtService.extractAdminType(token);

            String email = jwtService.extractEmail(token);

            AssignedService assignedService = jwtService.extractAssignedService(token);



            if (role == null || role.isBlank()) {

                role = adminType != null && !adminType.isBlank() ? adminType : "MAIN_ADMIN";

            }

            if (adminType == null || adminType.isBlank()) {

                adminType = role;

            }



            AdminPrincipal principal = new AdminPrincipal(adminId, email, role, adminType, assignedService, token);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,

                    token, principal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return true;

        } catch (Exception e) {

            SecurityContextHolder.clearContext();

            return false;

        }

    }



    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(status);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write("{\"success\":false,\"message\":\"" + message.replace("\"", "\\\"") + "\"}");

    }

}

