package com.example.employeeManagementSystem.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record EmployeeRequestDto (
        @NotBlank String name,
        @Positive Integer salary,
        String department,
        @Email String email
){

}