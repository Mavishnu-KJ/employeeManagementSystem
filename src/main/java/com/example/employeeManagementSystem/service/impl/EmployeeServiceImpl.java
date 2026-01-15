package com.example.employeeManagementSystem.service.impl;

import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.repository.EmployeeRepository;
import com.example.employeeManagementSystem.model.entity.Employee;
import com.example.employeeManagementSystem.service.EmployeeService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper mapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, ModelMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }


    @Transactional
    public EmployeeResponseDto create(EmployeeRequestDto employeeRequestDto){
        System.out.println("employeeRequestDto is "+employeeRequestDto);
        //Employee employee = mapper.map(employeeRequestDto, Employee.class); //ModelMapper did not work
        //System.out.println("employee is "+employee);
        Employee employee = new Employee();
        employee.setName(employeeRequestDto.name());
        employee.setSalary(employeeRequestDto.salary());
        employee.setDepartment(employeeRequestDto.department());
        employee.setEmail(employeeRequestDto.email());
        //System.out.println("Employee is "+employee);
        Employee saved = employeeRepository.save(employee);
        //System.out.println("Employed saved is "+saved);
        return mapper.map(saved, EmployeeResponseDto.class);
    }

    public List<EmployeeResponseDto> getAll(){
        return employeeRepository.findAll().stream()
                .map(employee->mapper.map(employee,EmployeeResponseDto.class))
                .toList();
    }

    public EmployeeResponseDto getById(Long id) throws IOException {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new IOException("Employee not found with id: " + id));
        return mapper.map(employee, EmployeeResponseDto.class);
    }

    public EmployeeResponseDto searchEmployee(Long id) throws IOException {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new IOException("Employee not found with id: " + id));
        return mapper.map(employee, EmployeeResponseDto.class);
    }

    public EmployeeResponseDto updateEmployeeById(EmployeeRequestDto employeeRequestDto, Long id){
        Employee employee = employeeRepository.findById(id)
                .orElse(new Employee());

        employee.setName(employeeRequestDto.name());
        employee.setSalary(employeeRequestDto.salary());
        employee.setDepartment(employeeRequestDto.department());
        employee.setEmail(employeeRequestDto.email());

        Employee saved = employeeRepository.save(employee);
        return mapper.map(saved, EmployeeResponseDto.class);

    }



}
