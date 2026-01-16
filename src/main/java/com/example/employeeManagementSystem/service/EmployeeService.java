package com.example.employeeManagementSystem.service;

import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.model.entity.Employee;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface EmployeeService {
    EmployeeResponseDto addEmployee(EmployeeRequestDto employeeRequestDto);
    EmployeeResponseDto getEmployeeById(Long id);
    List<EmployeeResponseDto> getAllEmployees();
    EmployeeResponseDto searchEmployeeById(Long id);
    EmployeeResponseDto updateEmployeeById(EmployeeRequestDto employeeRequestDto, Long id);
    List<EmployeeResponseDto> updateEmployeeByName(EmployeeRequestDto employeeRequestDto, String name);
    List<EmployeeResponseDto> addEmployees(List<EmployeeRequestDto> employeeRequestDtoList);
    void deleteEmployeeById(Long id);
}
