package com.example.employeeManagementSystem.model.dto;

public record EmployeeResponseDto (
        Long id,
        String name,
        Integer salary,
        String department,
        String email
)
{
}
