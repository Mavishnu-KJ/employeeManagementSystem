package com.example.employeeManagementSystem.controller;

import com.example.employeeManagementSystem.exception.DuplicateEmailException;
import com.example.employeeManagementSystem.exception.ResourceNotFoundException;
import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean //@MockBean is also working but deprecated
    EmployeeService employeeService;

    @Autowired
    ObjectMapper objectMapper;

    @Test //Success - Valid request → 201 Created with Location header & body
    void testAddEmployee_Success() throws Exception{

        //Prepare request DTO
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto("Sachin", 900000, "Cricket", "sachin@gmail.com");

        //Prepare expected response DTO
        EmployeeResponseDto employeeResponseDto = new EmployeeResponseDto(1L, "Sachin", 900000, "Cricket", "sachin@gmail.com");

        //Mock service behavior
        when(employeeService.addEmployee(any(EmployeeRequestDto.class))).thenReturn(employeeResponseDto);

        //Perform POST request
        mockMvc.perform(post("/api/employees/addEmployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto)) //Converts Java DTO to JSON string for request body
                )
                .andExpect(status().isCreated()) //201 created
                .andExpect(header().string("location", containsString("api/employees/1")))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Sachin"))
                .andExpect(jsonPath("$.salary").value(900000))
                .andExpect(jsonPath("$.department").value("Cricket"))
                .andExpect(jsonPath("$.email").value("sachin@gmail.com"));

        //Verify service was called once
        verify(employeeService, times(1)).addEmployee(any(EmployeeRequestDto.class));
    }

    @Test // Test 2: Validation Failure - Invalid DTO (blank name) → 400 Bad Request
    void testAddEmployee_ValidationFailure_BlankName() throws Exception{

        //Prepare request DTO
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto("", 10000, "Cricket", "Rinku@gmail.com");

        //Since its a validation failure test case, skip expected response DTO

        //Since its DTO level validation failure, skip mocking service behavior

        //Perform POST request
        mockMvc.perform(post("/api/employees/addEmployee")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(employeeRequestDto))
                ).andExpect(status().isBadRequest())
                 .andExpect(jsonPath("$.name").value("must not be blank"));

        //Verify service was never called
        verify(employeeService, never()).addEmployee(any(EmployeeRequestDto.class));

    }

    @Test // Test 3: Service throws business exception → custom status (e.g., duplicate email)
    void testAddEmployee_ServiceThrowsException() throws Exception{
        //Prepare request DTO
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto(
                "Sachin",
                120000,
                "Cricket",
                "sachin@gmail.com"
        );

        //Mock service behavior
        when(employeeService.addEmployee(any(EmployeeRequestDto.class))).thenThrow(new DuplicateEmailException());

        //Perform post request
        mockMvc.perform(post("/api/employees/addEmployee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDto))
                        ).andExpect(status().isConflict())
                         .andExpect(jsonPath("$.message").value("Email already exists"));

        //Verify service was called once
        verify(employeeService, times(1)).addEmployee(any(EmployeeRequestDto.class));
    }

    @Test
    void testAddEmployees_Success() throws Exception{

        //Prepare request DTO
        List<EmployeeRequestDto> employeeRequestDtoList = List.of(
                new EmployeeRequestDto("Sachin", 88888, "Cricket", "sachin@gmail.com"),
                new EmployeeRequestDto("Virat", 22222, "Cricket", "virat@gmail.com")
        );

        //Prepare Response DTO
        List<EmployeeResponseDto> employeeResponseDtoList = new ArrayList<>();
        EmployeeResponseDto employeeResponseDto1 = new EmployeeResponseDto(1L, "Sachin", 88888, "Cricket", "sachin@gmail.com");
        EmployeeResponseDto employeeResponseDto2 = new EmployeeResponseDto(2L, "Virat", 22222, "Cricket", "virat@gmail.com");
        employeeResponseDtoList.add(employeeResponseDto1);
        employeeResponseDtoList.add(employeeResponseDto2);

        //Mock service behavior
        //when(employeeService.addEmployees(any(List<EmployeeRequestDto.class>))).thenReturn(employeeResponseDtoList);
        when(employeeService.addEmployees(anyList())).thenReturn(employeeResponseDtoList);

        //Perform POST request
        mockMvc.perform(post("/api/employees/addEmployees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDtoList))
                        ).andExpect(status().isCreated())
                         .andExpect(header().exists("Location"))
                         .andExpect(header().string("Location", containsString("/api/employees/addEmployees")))
                         .andExpect(jsonPath("$").isArray())
                         .andExpect(jsonPath("$").isNotEmpty())
                         .andExpect(jsonPath("$.length()").value(2))
                         .andExpect(jsonPath("$[0].name").value("Sachin"))
                         .andExpect(jsonPath("$[1].name").value("Virat"));

        //Verify the service was called once
        verify(employeeService, times(1)).addEmployees(anyList());

    }

    @Test
    void testAddEmployees_ValidationFailure_InvalidEmail() throws Exception{

        //Prepare request DTO
        List<EmployeeRequestDto> employeeRequestDtoList = List.of(
          new EmployeeRequestDto("Sachin", 88888, "Cricket", "Sachin@gmail.com"),
          new EmployeeRequestDto("Virat", 22222, "Cricket", "Virat Kohli mail ID")
        );

        //Perform post request
        mockMvc.perform(post("/api/employees/addEmployees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDtoList))
                ).andExpect(status().isBadRequest()) //status 400
                 .andExpect(jsonPath("$.email").value("must be a well-formed email address"));

        //Verify service was never called
        verify(employeeService, never()).addEmployees(anyList());

    }

    @Test
    void testAddEmployees_ServiceThrowsException() throws Exception{
        //Prepare request Body
        List<EmployeeRequestDto> employeeRequestDtoList = List.of(
                new EmployeeRequestDto("Sachin", 88888, "Cricket", "Sachin@gmail.com"),
                new EmployeeRequestDto("Virat", 22222, "Cricket", "Virat@gmail.com")
        );

        //Mock service behavior
        when(employeeService.addEmployees(anyList())).thenThrow(new DuplicateEmailException());

        //Perform post request
        mockMvc.perform(post("/api/employees/addEmployees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDtoList))
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));

        //Verify service was called once
        verify(employeeService, times(1)).addEmployees(any()); // NOTE : we can use either anyList() or any(), but any() is more generic and commonly used for lists

    }

    @Test
    void testGetEmployeeById_Success() throws Exception{

        //Prepare payload request
        Long id = 1L;

        //Prepare Response DTO
        EmployeeResponseDto employeeResponseDto = new EmployeeResponseDto(1L, "Sachin", 88888,"Cricket", "sachin@gmail.com");

        //Mock service behavior
        when(employeeService.getEmployeeById(anyLong())).thenReturn(employeeResponseDto);

        //Perform GET request
        mockMvc.perform(get("/api/employees/{id}", id)
                ).andExpect(status().isOk())
                 .andExpect(jsonPath("$.id").value(1L))
                 .andExpect(jsonPath("$.name").value("Sachin"))
                 .andExpect(jsonPath("$.salary").value(88888))
                 .andExpect(jsonPath("$.department").value("Cricket"))
                 .andExpect(jsonPath("$.email").value("sachin@gmail.com"));

        //Verify service was called once
        verify(employeeService, times(1)).getEmployeeById(eq(id));

    }

    @Test
    void testGetEmployeeById_ValidationFailure() throws Exception{

        //Prepare payload request
        String invalidId = "abc";

        //Perform GET request
        mockMvc.perform(get("/api/employees/{id}", invalidId)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid parameter : id Expected type Long but, got "+invalidId));

        //Verify service was never called
        verify(employeeService, never()).getEmployeeById(anyLong());
    }

    @Test
    void testGetEmployeeById_ServiceThrowsException() throws Exception{

        //Prepare payload request
        Long id = 999L;

        //Mock service behavior
        when(employeeService.getEmployeeById(anyLong())).thenThrow(new ResourceNotFoundException());

        //Perform GET request
        mockMvc.perform(get("/api/employees/{id}", id)
                ).andExpect(status().isNotFound())
                 .andExpect(jsonPath("$.message").value("Resource not found"));

        //Verify the service was called once
        verify(employeeService, times(1)).getEmployeeById(eq(id));

    }


}