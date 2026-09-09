package com.example.onboarding.dto;

import com.example.onboarding.entity.Employee;
import com.example.onboarding.entity.EmployeeStatus;
import com.example.onboarding.entity.EmploymentType;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        String email,
        String phone,
        String department,
        String jobTitle,
        String managerName,
        LocalDate joiningDate,
        EmploymentType employmentType,
        EmployeeStatus status,
        int totalTasks,
        int completedTasks,
        int progressPercentage
) {
    public static EmployeeResponse from(Employee employee) {
        int total = employee.getTasks().size();
        int completed = (int) employee.getTasks().stream().filter(task -> task.isCompleted()).count();
        int progress = total == 0 ? 0 : (completed * 100) / total;

        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDepartment(),
                employee.getJobTitle(),
                employee.getManagerName(),
                employee.getJoiningDate(),
                employee.getEmploymentType(),
                employee.getStatus(),
                total,
                completed,
                progress
        );
    }
}
