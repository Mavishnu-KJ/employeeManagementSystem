package com.example.employeeManagementSystem.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmployeeRequestDto (
        @NotBlank String name, //@NotBlank covers null check as well for String
        @NotNull @Positive Integer salary, //@NotNull should be used since it is Integer. If we us @NotBlank, it will throw unexpected error even for proper input values
        String department,
        @NotBlank @Email String email //@NotBlank covers null check as well for String
){

}