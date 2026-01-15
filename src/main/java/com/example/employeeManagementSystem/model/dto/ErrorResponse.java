package com.example.employeeManagementSystem.model.dto;

import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

public record ErrorResponse (
       int statusCode,
       String message,
       LocalDateTime timestamp
){
}
