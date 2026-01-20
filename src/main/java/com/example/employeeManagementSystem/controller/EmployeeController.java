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

    @PostMapping("/addEmployees") //http://localhost:8080/api/employees/addEmployees
    public ResponseEntity<List<EmployeeResponseDto>> addEmployees (@RequestBody List<EmployeeRequestDto> employeeRequestDtoList){
        logger.info("addEmployees, employeeRequestDtoList is {}", employeeRequestDtoList);
        List<EmployeeResponseDto> employeesAdded = employeeService.addEmployees(employeeRequestDtoList);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().build().toUri();

        logger.info("addEmployees, employeesAdded is {}", employeesAdded);

        //return ResponseEntity.status(HttpStatus.CREATED).body(employeesAdded);
        return ResponseEntity.created(location).body(employeesAdded);

    }

    @GetMapping("/{id}") //http://localhost:8080/api/employees/{id}
    EmployeeResponseDto getEmployeeById(@PathVariable Long id){
        logger.info("getEmployeeById, id is {}", id);
        return employeeService.getEmployeeById(id);
    }

    @GetMapping //http://localhost:8080/api/employees
    public List<EmployeeResponseDto> getAllEmployees(){
        return employeeService.getAllEmployees();
    }

    @GetMapping("/searchEmployeeById") //http://localhost:8080/api/employees/searchEmployeeById?id={id}
    EmployeeResponseDto searchEmployeeById(@Valid @RequestParam("id") Long id){
        return employeeService.searchEmployeeById(id);
    }

    //http://localhost:8080/api/employees/searchEmployees?name={name}
    //http://localhost:8080/api/employees/searchEmployees?department={department}
    //http://localhost:8080/api/employees/searchEmployees?minSalary={minSalary}
    //http://localhost:8080/api/employees/searchEmployees?department={department}&minSalary={minSalary}
    //http://localhost:8080/api/employees/searchEmployees
    @GetMapping("/searchEmployees")
    public ResponseEntity<List<EmployeeResponseDto>> searchEmployees(@RequestParam(name="name", required = false) String employeeName,
                                              @RequestParam(name="department", required = false) String department,
                                              @RequestParam(name="minSalary", required = false) Integer minSalary){
        logger.info("searchEmployees, name is {}, department is {}, minSalary is {}", employeeName, department, minSalary);
        List<EmployeeResponseDto> resultList = employeeService.searchEmployees(employeeName, department, minSalary);

        logger.info("searchEmployees, resultList is {}", resultList);
        return ResponseEntity.ok(resultList);

    }


    @PutMapping("/updateEmployeeById/{id}") //http://localhost:8080/api/employees/updateEmployeeById/10
    public ResponseEntity<EmployeeResponseDto> updateEmployeeById(@Valid @RequestBody EmployeeRequestDto employeeRequestDto, @PathVariable Long id){
        logger.info("updateEmployeeById, employeeRequestDto is {} and id is {}", employeeRequestDto, id);
        EmployeeResponseDto updated = employeeService.updateEmployeeById(employeeRequestDto, id);
        logger.info("updateEmployeeById, updated is {}", updated);
        //return ResponseEntity.status(HttpStatus.OK).body(updated);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/updateEmployeeByName/{name}") //http://localhost:8080/api/employees/updateEmployeeByName/Sachin
    public ResponseEntity<List<EmployeeResponseDto>> updateEmployeeByName(@Valid @RequestBody EmployeeRequestDto employeeRequestDto, @PathVariable ("name") String name){
        logger.info("updateEmployeeByName, employeeRequestDto is {} and name is {}", employeeRequestDto, name);
        List<EmployeeResponseDto> updatedList = employeeService.updateEmployeeByName(employeeRequestDto, name);

        if(updatedList==null || updatedList.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        logger.info("updateEmployeeByName, updatedList is {}", updatedList);
        return ResponseEntity.ok(updatedList);

    }

    @DeleteMapping("/deleteEmployeeById/{id}") //http://localhost:8080/api/employees/deleteEmployeeById/4
    public ResponseEntity<HttpStatus> deleteEmployeeById(@PathVariable("id") Long id){
        employeeService.deleteEmployeeById(id);

        return ResponseEntity.noContent().build(); //204 - No content
    }


    @GetMapping("/getAllEmployeesWithPagination")
    public Page<EmployeeResponseDto> getAllEmployeesWithPagination(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        logger.info("getAllEmployeesWithPagination, pageable is {}", pageable);
        return employeeService.getAllEmployeesWithPagination(pageable);
    }

    @GetMapping("/searchEmployeesWithPagination")
    public Page<EmployeeResponseDto> searchEmployeesWithPagination(@RequestParam(name="name", required = false) String employeeName,
                                                                   @RequestParam(name="department", required=false) String department,
                                                                   @RequestParam(name="minSalary", required=false) Integer minSalary,
                                                                   Pageable pageable){


        return employeeService.searchEmployeesWithPagination(employeeName, department, minSalary, pageable);
    }

}
