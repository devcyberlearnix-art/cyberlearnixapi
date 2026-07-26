package com.example.admin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AdminSecurityContext {

    private AdminSecurityContext() {}

    public static AdminPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof AdminPrincipal adminPrincipal) {
            return adminPrincipal;
        }
        return null;
    }

    public static String getToken() {
        AdminPrincipal principal = getPrincipal();
        return principal != null ? principal.token() : null;
    }
}
