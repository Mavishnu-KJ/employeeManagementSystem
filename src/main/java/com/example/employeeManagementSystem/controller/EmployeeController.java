package com.example.employeeManagementSystem.controller;

import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/addEmployee")
    public ResponseEntity<EmployeeResponseDto> addEmployee(@Valid @RequestBody EmployeeRequestDto employeeRequestDto){
        EmployeeResponseDto saved = employeeService.addEmployee(employeeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}") //http://localhost:8080/api/employees/{id}
    EmployeeResponseDto getEmployeeById(@PathVariable Long id){
        return employeeService.getEmployeeById(id);
    }

    @GetMapping
    public List<EmployeeResponseDto> getAllEmployees(){
        return employeeService.getAllEmployees();
    }

    @GetMapping("/searchEmployeeById") //http://localhost:8080/api/employees/searchEmployeeById?id={id}&name={name}
    EmployeeResponseDto searchEmployeeById(@Valid @RequestParam("id") Long id){
        return employeeService.searchEmployeeById(id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployeeById(@Valid @RequestBody EmployeeRequestDto employeeRequestDto, @PathVariable Long id){
        EmployeeResponseDto updated = employeeService.updateEmployeeById(employeeRequestDto, id);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

}
