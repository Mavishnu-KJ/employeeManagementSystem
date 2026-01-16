package com.example.employeeManagementSystem.service.impl;

import com.example.employeeManagementSystem.exception.ResourceNotFoundException;
import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.repository.EmployeeRepository;
import com.example.employeeManagementSystem.model.entity.Employee;
import com.example.employeeManagementSystem.service.EmployeeService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper mapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, ModelMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }


    @Transactional
    public EmployeeResponseDto addEmployee(EmployeeRequestDto employeeRequestDto){
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

    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Employee not found with id: " + id));
        return mapper.map(employee, EmployeeResponseDto.class);
    }

    public List<EmployeeResponseDto> getAllEmployees(){
        return employeeRepository.findAll().stream()
                .map(employee->mapper.map(employee,EmployeeResponseDto.class))
                .toList();
    }

    public EmployeeResponseDto searchEmployeeById(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Employee not found with id: " + id));
        return mapper.map(employee, EmployeeResponseDto.class);
    }

    @Transactional
    public EmployeeResponseDto updateEmployeeById(EmployeeRequestDto employeeRequestDto, Long id){
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Resource not found for the given id: "+id));

        existingEmployee.setName(employeeRequestDto.name());
        existingEmployee.setSalary(employeeRequestDto.salary());
        existingEmployee.setDepartment(employeeRequestDto.department());
        existingEmployee.setEmail(employeeRequestDto.email());

        Employee updated = employeeRepository.save(existingEmployee);
        return mapper.map(updated, EmployeeResponseDto.class);

    }

    @Override
    @Transactional
    public List<EmployeeResponseDto> updateEmployeeByName(EmployeeRequestDto employeeRequestDto, String name) {
        List<Employee> existingEmployeeList = employeeRepository.findByName(name);

        if(existingEmployeeList.isEmpty()){
            throw new ResourceNotFoundException("Resource not found for the name : "+name);
        }

        List<EmployeeResponseDto> employeeResponseDtoList = new ArrayList<>();

        for(Employee existingEmployee : existingEmployeeList){
            existingEmployee.setSalary(employeeRequestDto.salary());
            existingEmployee.setDepartment(employeeRequestDto.department());
            existingEmployee.setEmail(employeeRequestDto.email());
            existingEmployee.setName(employeeRequestDto.name());

            //save
            Employee saved = employeeRepository.save(existingEmployee);

            //map to dto
            employeeResponseDtoList.add(mapToResponseDto(saved));

        }

        return employeeResponseDtoList;

    }

    public EmployeeResponseDto mapToResponseDto(Employee employee){
        EmployeeResponseDto employeeResponseDto = new EmployeeResponseDto();
        employeeResponseDto.setName(employee.getName());
        employeeResponseDto.setSalary(employee.getSalary());
        employeeResponseDto.setDepartment(employee.getDepartment());
        employeeResponseDto.setEmail(employee.getEmail());
        if(employee.getId()!=null) employeeResponseDto.setId(employee.getId());
        return employeeResponseDto;
    }

    public Employee mapEmployeeRequestDtoToEmployeeEntity(EmployeeRequestDto employeeRequestDto){
        Employee employee = new Employee();
        employee.setName(employeeRequestDto.name());
        employee.setSalary(employeeRequestDto.salary());
        employee.setDepartment(employeeRequestDto.department());
        employee.setEmail(employeeRequestDto.email());
        //employee.setId(employeeRequestDto.getId());
        return employee;
    }

    @Override
    @Transactional
    public List<EmployeeResponseDto> addEmployees(List<EmployeeRequestDto> employeeRequestDtoList){
        List<EmployeeResponseDto> employeeResponseDtoList = new ArrayList<EmployeeResponseDto>();

        List<Employee> employeeList = new ArrayList<Employee>();

        if(employeeRequestDtoList == null || employeeRequestDtoList.isEmpty()){
            throw new ResourceNotFoundException("No Employees data found to add");
        }

        for(EmployeeRequestDto employeeRequestDto : employeeRequestDtoList){
            employeeList.add(mapEmployeeRequestDtoToEmployeeEntity(employeeRequestDto));
        }

        //save
        List<Employee> employeeListAdded = employeeRepository.saveAll(employeeList);

        //Map to ResponseDto
        if(employeeListAdded !=null && !employeeListAdded.isEmpty()){
            for(Employee emp : employeeListAdded){
                employeeResponseDtoList.add(mapToResponseDto(emp));
            }
        }

        return employeeResponseDtoList;
    }

    @Override
    @Transactional
    public void deleteEmployeeById(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Resource not found for the Id: "+id));

        //Delete
        employeeRepository.delete(employee);

    }


}
