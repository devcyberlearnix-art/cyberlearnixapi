package com.example.admin.dto;

import lombok.Data;

@Data
public class UserReportDto {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long newUsersLast7Days;
}