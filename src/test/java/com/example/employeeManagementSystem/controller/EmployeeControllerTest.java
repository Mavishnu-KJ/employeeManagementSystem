package com.example.employeeManagementSystem.controller;

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

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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



}