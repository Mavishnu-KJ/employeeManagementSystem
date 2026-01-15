package com.example.employeeManagementSystem.service;

import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.model.entity.Employee;

import java.io.IOException;
import java.util.List;

public interface EmployeeService {
    EmployeeResponseDto create(EmployeeRequestDto employeeRequestDto);
    List<EmployeeResponseDto> getAll();
    EmployeeResponseDto getById(Long id) throws IOException;
    EmployeeResponseDto searchEmployee(Long id) throws IOException;
    EmployeeResponseDto updateEmployeeById(EmployeeRequestDto employeeRequestDto, Long id);
}
