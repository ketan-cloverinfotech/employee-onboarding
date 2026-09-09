package com.example.onboarding.dto;

public record DashboardStats(
        long totalEmployees,
        long preBoarding,
        long inProgress,
        long active,
        long pendingTasks
) {
}
