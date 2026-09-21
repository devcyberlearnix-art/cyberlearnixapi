package com.example.admin.security;

import com.example.admin.entity.AssignedService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AdminPrincipal implements org.springframework.security.core.userdetails.UserDetails {

    private final UUID adminId;
    private final String email;
    private final String role;
    private final String adminType;
    private final AssignedService assignedService;
    private final String token;

    public AdminPrincipal(UUID adminId, String email, String role, String adminType,
                        AssignedService assignedService, String token) {
        this.adminId = adminId;
        this.email = email;
        this.role = role;
        this.adminType = adminType;
        this.assignedService = assignedService;
        this.token = token;
    }

    // Constructor for backward compatibility
    public AdminPrincipal(UUID adminId, String role, String adminType,
                        AssignedService assignedService, String token) {
        this(adminId, null, role, adminType, assignedService, token);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        String r = role != null ? role : "MAIN_ADMIN";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_MAIN_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_SUB_ADMIN"));

        if (assignedService != null && assignedService != AssignedService.ALL) {
            authorities.add(new SimpleGrantedAuthority("SERVICE_" + assignedService.name()));
        } else {
            authorities.add(new SimpleGrantedAuthority("SERVICE_ALL"));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Not used for authentication
    }

    @Override
    public String getUsername() {
        return email != null ? email : adminId.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Getters
    public UUID getAdminId() { return adminId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAdminType() { return adminType; }
    public AssignedService getAssignedService() { return assignedService; }
    public String getToken() { return token; }
}
