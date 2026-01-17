package com.example.employeeManagementSystem.controller;

import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/addEmployee") //http://localhost:8080/api/employees/addEmployee
    public ResponseEntity<EmployeeResponseDto> addEmployee(@Valid @RequestBody EmployeeRequestDto employeeRequestDto){
        logger.info("addEmployee, employeeRequestDto is {}", employeeRequestDto);

        EmployeeResponseDto added = employeeService.addEmployee(employeeRequestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replacePath("/api/employees/{id}")
                .buildAndExpand(added.getId())
                .toUri();

        logger.info("addEmployee, employee added is {}", added);

        //return ResponseEntity.status(HttpStatus.CREATED).body(added);
        return ResponseEntity.created(location).body(added); // 201 created
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

    @PutMapping("/updateEmployeeById/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployeeById(@Valid @RequestBody EmployeeRequestDto employeeRequestDto, @PathVariable Long id){
        EmployeeResponseDto updated = employeeService.updateEmployeeById(employeeRequestDto, id);
        //return ResponseEntity.status(HttpStatus.OK).body(updated);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/updateEmployeeByName/{name}")
    public ResponseEntity<List<EmployeeResponseDto>> updateEmployeeByName(@Valid @RequestBody EmployeeRequestDto employeeRequestDto, @PathVariable ("name") String name){

        List<EmployeeResponseDto> updatedList = employeeService.updateEmployeeByName(employeeRequestDto, name);

        if(updatedList==null || updatedList.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedList);

    }

    @PostMapping("/addEmployees")
    public ResponseEntity<List<EmployeeResponseDto>> addEmployees (@RequestBody List<EmployeeRequestDto> employeeRequestDtoList){

        logger.info("Controller entered addEmployees, employeeRequestDtoList is {}",employeeRequestDtoList);
        List<EmployeeResponseDto> employeesAdded = employeeService.addEmployees(employeeRequestDtoList);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().build().toUri();
        //return ResponseEntity.ok(employeesAdded);
        //return ResponseEntity.status(HttpStatus.CREATED).body(employeesAdded);
        return ResponseEntity.created(location).body(employeesAdded);

    }

    @DeleteMapping("/deleteEmployeeById/{id}")
    public ResponseEntity<HttpStatus> deleteEmployeeById(@PathVariable("id") Long id){
        employeeService.deleteEmployeeById(id);

        return ResponseEntity.noContent().build(); //204 - No content
    }


    @GetMapping("/getAllEmployeesWithPagination")
    public Page<EmployeeResponseDto> getAllEmployeesWithPagination(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){

        return employeeService.getAllEmployeesWithPagination(pageable);
    }

}
