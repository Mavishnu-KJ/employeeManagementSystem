package com.example.employeeManagementSystem.controller;

import com.example.employeeManagementSystem.exception.DuplicateEmailException;
import com.example.employeeManagementSystem.exception.ResourceNotFoundException;
import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    @DisplayName("GET /api/employees - Success with multiple employees")
    void testGetAllEmployees_Success() throws Exception{

        //Prepare Response
        List<EmployeeResponseDto> employeeResponseDtoList = List.of(
                new EmployeeResponseDto(1L, "Sachin", 88888, "Cricket", "sachin@gmail.com"),
                new EmployeeResponseDto(2L, "Virat", 22222, "Cricket", "virat@gmail.com")
        );

        //Mock service behavior
        when(employeeService.getAllEmployees()).thenReturn(employeeResponseDtoList);

        //Perform GET request
        mockMvc.perform(get("/api/employees")
                ).andExpect(status().isOk())
                 .andExpect(jsonPath("$").isArray())
                 .andExpect(jsonPath("$").isNotEmpty())
                 .andExpect(jsonPath("$.length()").value(2))
                 .andExpect(jsonPath("$[0].name").value("Sachin"))
                 .andExpect(jsonPath("$[1].name").value("Virat"));

        //Verify the service was called once
        verify(employeeService, times(1)).getAllEmployees();

    }

    @Test
    @DisplayName("GET /api/employees - Success with empty list")
    void testGetAllEmployees_SuccessWithEmptyList() throws Exception{

        //Prepare Response
        List<EmployeeResponseDto> employeeResponseDtoList = List.of();

        //Mock service behavior
        when(employeeService.getAllEmployees()).thenReturn(employeeResponseDtoList);

        //Perform GET request
        mockMvc.perform(get("/api/employees")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andExpect(jsonPath("$.length()").value(0));

        //Verify the service was called once
        verify(employeeService, times(1)).getAllEmployees();

    }

    @Test
    @DisplayName("GET /api/employees - Service throws Exception - 500")
    void testGetAllEmployees_ServiceThrowsException() throws Exception{

        //Mock service behavior
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Database error"));

        //Perform GET request
        mockMvc.perform(get("/api/employees")
                ).andExpect(status().isInternalServerError());

        //Verify the service was called once
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void testSearchEmployeeById_Success() throws Exception{
        //Prepare payload request
        Long id = 1L;

        //Prepare response
        EmployeeResponseDto employeeResponseDto = new EmployeeResponseDto(
                1L,
                "Sachin",
                88888,
                "Cricket",
                "sachin@gmail.com"
        );

        //Mock service behavior
        when(employeeService.searchEmployeeById(anyLong())).thenReturn(employeeResponseDto);

        //Perform GET request
        mockMvc.perform(get("/api/employees/searchEmployeeById").queryParam("id", String.valueOf(id))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Sachin"))
                .andExpect(jsonPath("$.salary").value(88888))
                .andExpect(jsonPath("$.department").value("Cricket"))
                .andExpect(jsonPath("$.email").value("sachin@gmail.com"));

        //Verify the service was called once
        verify(employeeService, times(1)).searchEmployeeById(id);
    }

    @Test
    void testSearchEmployeeById_ValidationFailure() throws Exception{
        //Prepare invalid payload request
        String id = "abc";

        //Perform GET request
        mockMvc.perform(get("/api/employees/searchEmployeeById").queryParam("id", id)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid parameter : id Expected type Long but, got abc"));

        //Verify the service was never called
        verify(employeeService, never()).searchEmployeeById(anyLong());
    }


    @Test
    void testSearchEmployeeById_ServiceThrowsException() throws Exception{
        //Prepare payload request
        Long id = 999L;

        //Mock service behavior
        when(employeeService.searchEmployeeById(anyLong())).thenThrow(new ResourceNotFoundException("Employee not found with id: " + id));

        //Perform GET request
        mockMvc.perform(get("/api/employees/searchEmployeeById").queryParam("id", String.valueOf(id))
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found with id: " + id));

        //Verify the service was called once
        verify(employeeService, times(1)).searchEmployeeById(eq(id));
    }

    @Test
    @DisplayName("GET http://localhost:8080/api/employees/searchEmployees?name={name}")
    void testSearchEmployees_Success() throws Exception{

        //Prepare payload request
        String name = "Sachin";

        //Prepare response
        List<EmployeeResponseDto> employeeResponseDtoList = List.of(
                new EmployeeResponseDto(1L, "Sachin", 88888, "Cricket", "sachin@gmail.com"),
                new EmployeeResponseDto(2L, "Sachin Tendulkar", 22222, "Cricket", "sachintendulkar@gmail.com")
        );

        //Mock service behavior
        when(employeeService.searchEmployees(eq(name), isNull(), isNull())).thenReturn(employeeResponseDtoList);

        //Perform GET request
        mockMvc.perform(get("/api/employees/searchEmployees").queryParam("name", name)
                 .accept(MediaType.APPLICATION_JSON)
                 ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Sachin"))
                .andExpect(jsonPath("$[1].name").value("Sachin Tendulkar"));

        //Verify the service was called once
        verify(employeeService, times(1)).searchEmployees(eq(name), isNull(), isNull());

    }

    @Test
    @DisplayName("GET http://localhost:8080/api/employees/searchEmployees?department={department}&minSalary={minSalary}")
    void testSearchEmployees_Success1() throws Exception{
        //Prepare payload request
        String department = "Cricket";
        Integer minSalary = 22222;

        //Prepare response
        List<EmployeeResponseDto> employeeResponseDtoList = List.of(
                new EmployeeResponseDto(1L, "Sachin", 88888, "Cricket", "sachin@gmail.com"),
                new EmployeeResponseDto(2L, "Sachin Tendulkar", 22222, "Cricket", "sachintendulkar@gmail.com")
        );

        //Mock service behavior
        when(employeeService.searchEmployees(isNull(), eq(department), eq(minSalary))).thenReturn(employeeResponseDtoList);

        //Perform GET request
        mockMvc.perform(get("/api/employees/searchEmployees")
                    .queryParam("department", department)
                    .queryParam("minSalary", String.valueOf(minSalary))
                    .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Sachin"))
                .andExpect(jsonPath("$[1].name").value("Sachin Tendulkar"));

        //Verify the service was called once
        verify(employeeService, times(1)).searchEmployees(isNull(), eq(department), eq(minSalary));

    }

    @Test
    @DisplayName("GET http://localhost:8080/api/employees/searchEmployees?name={name}&department={department}&minSalary={minSalary}")
    void testSearchEmployees_Success2() throws Exception{
        //Prepare payload request
        String name = "Sachin";
        String department = "Cricket";
        Integer minSalary = 22222;

        //Prepare response
        List<EmployeeResponseDto> employeeResponseDtoList = List.of(
                new EmployeeResponseDto(1L, "Sachin", 88888, "Cricket", "sachin@gmail.com"),
                new EmployeeResponseDto(2L, "Sachin Tendulkar", 22222, "Cricket", "sachintendulkar@gmail.com")
        );

        //Mock service behavior
        when(employeeService.searchEmployees(eq(name), eq(department), eq(minSalary))).thenReturn(employeeResponseDtoList);

        //Using queryParams instead of queryParam
        MultiValueMap<String, String> queryParamsMap = new LinkedMultiValueMap<>();
        queryParamsMap.add("name", name);
        queryParamsMap.add("department", department);
        queryParamsMap.add("minSalary", String.valueOf(minSalary));

        //Perform GET request
        mockMvc.perform(get("/api/employees/searchEmployees")
                        .queryParams(queryParamsMap)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Sachin"))
                .andExpect(jsonPath("$[1].name").value("Sachin Tendulkar"));

        //Verify the service was called once
        verify(employeeService, times(1)).searchEmployees(eq(name), eq(department), eq(minSalary));

    }

    @Test
    @DisplayName("GET http://localhost:8080/api/employees/searchEmployees?minSalary={minSalary} negative input")
    void testSearchEmployees_ValidationFailure_NegativeSalary() throws Exception{

        //Prepare invalid payload request
        Integer negativeSalary = -15000;

        //Perform GET request
        mockMvc.perform(get("/api/employees/searchEmployees").queryParam("minSalary", String.valueOf(negativeSalary))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.global").value("minSalary must be positive"));

        //Verify the service was never called
        verify(employeeService, never()).searchEmployees(isNull(), isNull(), eq(negativeSalary));
    }

    @Test
    @DisplayName("GET http://localhost:8080/api/employees/searchEmployees?department={department}, SERVICE throws exception")
    void testSearchEmployees_ServiceThrowsException() throws Exception{
        //prepare payload request
        String department = "Cricket";

        //Mock service behavior
        when(employeeService.searchEmployees(isNull(), eq(department), isNull())).thenThrow(new RuntimeException("Database error"));

        //Perform GET request
        mockMvc.perform(get("/api/employees/searchEmployees").queryParam("department", department)
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Unexpected Error: Database error"));

        //Verify the service was called once
        verify(employeeService, times(1)).searchEmployees(isNull(), eq(department), isNull());

    }

    @Test
    void testUpdateEmployeeById_Success() throws Exception{
        //Prepare payload request
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto(
                "Sachin",
                88888,
                "Cricket",
                "sachin@gmail.com");

        Long id = 1L;

        //Prepare response DTO
        EmployeeResponseDto employeeResponseDto = new EmployeeResponseDto(
                1L,
                "Sachin",
                88888,
                "Cricket",
                "sachin@gmail.com"
        );

        //Mock service behavior
        when(employeeService.updateEmployeeById(any(), any())).thenReturn(employeeResponseDto);

        //Perform PUT request
        mockMvc.perform(put("/api/employees/updateEmployeeById/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Sachin"));

        //Verify the service was called once
        verify(employeeService, times(1)).updateEmployeeById(eq(employeeRequestDto),eq(id));
    }

    @Test
    void testUpdateEmployeeById_ValidationFailure() throws Exception{

        //Prepare invalid payload request
        Long id = 1L;
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto(
                "Pant",
                -100,
                "Cricket",
                "pant@gmail.com");

        //Perform PUT request
        mockMvc.perform(put("/api/employees/updateEmployeeById/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest()) //400
                .andExpect(jsonPath("$.salary").value("must be greater than 0"));

        //Verify service was never called
        verify(employeeService, never()).updateEmployeeById(eq(employeeRequestDto), eq(id));

    }

    @Test
    void testUpdateEmployeeById_ServiceThrowsException() throws Exception{
        //Prepare payload request
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto(
                "Sachin",
                88888,
                "Cricket",
                "sachin@gmail.com");

        Long id = 999L;

        //Mock service behavior
        when(employeeService.updateEmployeeById(eq(employeeRequestDto), eq(id))).thenThrow(
          new ResourceNotFoundException("Resource not found for the given id: "+id)
        );

        //Perform PUT request
        mockMvc.perform(put("/api/employees/updateEmployeeById/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNotFound()) //404
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Resource not found for the given id: "+id));

        //Verify the service was called once
        verify(employeeService, times(1)).updateEmployeeById(eq(employeeRequestDto), eq(id));

    }

    @Test
    void testUpdateEmployeeByName_Success() throws Exception{
        //Prepare payload request
        String name = "Sachin";
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto(
                "Sachin",
                88888,
                "Cricket",
                "sachin@gmail.com");

        //Prepare Mocked response
        List<EmployeeResponseDto> employeeResponseDtoList = List.of(
                new EmployeeResponseDto(1L, "Sachin", 88888, "Cricket", "sachin@gmail.com"),
                new EmployeeResponseDto(2L, "Sachin", 88888, "Cricket", "sachin@gmail.com")
        );

        //Mock service behavior
        when(employeeService.updateEmployeeByName(eq(employeeRequestDto), eq(name))).thenReturn(employeeResponseDtoList);

        //Perform PUT request
        mockMvc.perform(put("/api/employees/updateEmployeeByName/{name}", name)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Sachin"))
                .andExpect(jsonPath("$[1].name").value("Sachin"));

        //Verify the service was called once
        verify(employeeService, times(1)).updateEmployeeByName(eq(employeeRequestDto), eq(name));
    }

    @Test
    void testUpdateEmployeeByName_ValidationFailure() throws Exception{
        //Prepare invalid payload request
        String name = "Sachin";
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto(
                "",
                88888,
                "Cricket",
                "sachin@gmail.com");

        //Perform PUT request
        mockMvc.perform(put("/api/employees/updateEmployeeByName/{name}", name)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDto))
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("must not be blank"));

        //Verify the service was never called
        verify(employeeService, never()).updateEmployeeByName(eq(employeeRequestDto), eq(name));
    }

    @Test
    void testUpdateEmployeeByName_ServiceThrowsException() throws Exception{
        //Prepare invalid payload request
        String name = "Rajinikanth";
        EmployeeRequestDto employeeRequestDto = new EmployeeRequestDto(
                "Rajinikanth",
                88888,
                "Cricket",
                "Rajinikanth@gmail.com");

        //Mock service behavior
        when(employeeService.updateEmployeeByName(eq(employeeRequestDto),eq(name))).thenThrow(
          new ResourceNotFoundException("Resource not found for the name : "+name)
        );

        //Perform PUT request
        mockMvc.perform(put("/api/employees/updateEmployeeByName/{name}", name)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDto))
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Resource not found for the name : "+name));

        //Verify the service was called once
        verify(employeeService, times(1)).updateEmployeeByName(eq(employeeRequestDto), eq(name));
    }

    @Test
    void testDeleteEmployeeById_Success() throws Exception{
        //Prepare payload request
        Long id = 1L;

        //Mock service behavior
        doNothing().when(employeeService).deleteEmployeeById(eq(id)); //doNothing() as no return value needed

        //Perform DELETE request
        mockMvc.perform(delete("/api/employees/deleteEmployeeById/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNoContent()); //204

        //Verify service was called once
        verify(employeeService, times(1)).deleteEmployeeById(eq(id));
    }

    @Test
    void testDeleteEmployeeById_ValidationFailure() throws Exception{
        //Prepare payload request
        String invalidId = "abc";

        //Perform DELETE request
        mockMvc.perform(delete("/api/employees/deleteEmployeeById/{id}", invalidId)
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest()) //400
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid parameter : id Expected type Long but, got "+invalidId));

        //Verify the service was never called
        verify(employeeService, never()).deleteEmployeeById(anyLong());
    }

    @Test
    void testDeleteEmployeeById_ServiceThrowsException() throws Exception{
        //Prepare payload request
        Long id = 999L;

        //Mock service behavior
        doThrow(new ResourceNotFoundException("Resource not found for the Id: "+id)).when(employeeService).deleteEmployeeById(eq(id));

        //Perform DELETE request
        mockMvc.perform(delete("/api/employees/deleteEmployeeById/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNotFound()) //404
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Resource not found for the Id: "+id));

        //Verify the service was called once
        verify(employeeService, times(1)).deleteEmployeeById(eq(id));
    }

}