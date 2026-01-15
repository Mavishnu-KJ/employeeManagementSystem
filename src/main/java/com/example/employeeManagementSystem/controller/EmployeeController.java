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

    @PostMapping("/create")
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto employeeRequestDto){
        EmployeeResponseDto saved = employeeService.create(employeeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<EmployeeResponseDto> getAll(){
        return employeeService.getAll();
    }

    @GetMapping("/{id}") //http://localhost:8080/api/employees/{id}
    EmployeeResponseDto getById(@PathVariable Long id) throws IOException{

        return employeeService.getById(id);
    }

    @GetMapping("/search") //http://localhost:8080/api/employees/search?id={id}
    EmployeeResponseDto searchEmployee(@RequestParam("id") Long id) throws IOException{
        return employeeService.searchEmployee(id);
    }

    @PutMapping("/update/{id}")
    EmployeeResponseDto updateEmployeeById(@Valid @RequestBody EmployeeRequestDto employeeRequestDto, @PathVariable Long id){
        EmployeeResponseDto updated = employeeService.updateEmployeeById(employeeRequestDto, id);
        return ResponseEntity.status(HttpStatus.OK).body(updated).getBody();
    }

}
