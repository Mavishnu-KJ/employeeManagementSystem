package com.example.employeeManagementSystem.service;

import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



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
    Page<EmployeeResponseDto> getAllEmployeesWithPagination(Pageable pageable);
}
