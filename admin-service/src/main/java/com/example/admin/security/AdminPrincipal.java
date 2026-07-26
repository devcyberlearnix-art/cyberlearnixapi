package com.example.admin.security;

import com.example.admin.entity.AssignedService;

import java.util.UUID;

public record AdminPrincipal(
        UUID adminId,
        String role,
        String adminType,
        AssignedService assignedService,
        String token
) {}
