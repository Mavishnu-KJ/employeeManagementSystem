# Employee Management System (EMS)

A **full-featured RESTful API** built with Spring Boot for managing employees. It demonstrates modern Spring Boot best practices including JPA, validation, exception handling, Actuator monitoring, and clean architecture.

## Features
- Full CRUD operations for Employees
- Batch create multiple employees
- Search/filter employees (by name, department, min salary)
- Pagination & sorting support
- DTO pattern (no entity exposure)
- Bean Validation (`@Valid`, `@NotBlank`, `@Positive`, `@Email`)
- Global exception handling with custom error responses
- Actuator endpoints for monitoring (health, info, metrics)
- Manual mapping (no external mapper library issues)

## Tech Stack
- Java 17 / 25
- Spring Boot 3.5.9
- Spring Web
- Spring Data JPA
- H2 Database (in-memory)
- Spring Boot Actuator
- Spring Boot DevTools
- JUnit 5 + Mockito (tests)
- Lombok (optional)

## Prerequisites
- Java 17+ (JDK 17 or 25 recommended)
- Maven 3.6+
- Git
- IntelliJ IDEA / VS Code / Eclipse

## How to Run
1. Clone the repository
   ```bash
   git clone https://github.com/Mavishnu-KJ/employeeManagementSystem.git
   cd employeeManagementSystem
2. Build the project 
   mvn clean install
3. Run the application
   mvn spring-boot:run
   or from IntelliJ: Right-click EmployeeManagementSystemApplication.java → Run
4. H2 Console (optional): http://localhost:8080/h2-console
   JDBC URL: jdbc:h2:mem:testdb
   Username: sa
   Password: (blank)
5. API Base URL: http://localhost:8080/api/employees
6. API Endpoints 

  **@PostMapping("/addEmployee") //http://localhost:8080/api/employees/addEmployee
      public ResponseEntity<EmployeeResponseDto> addEmployee(@Valid @RequestBody EmployeeRequestDto employeeRequestDto)**
  
  => It is used to add one Employee at a time
  => ModelMapper did not work, So used manual mapping. Manual mapping is more reliable than ModelMapper
  => It uses the default method save(employee) (from JPARepository/CRUD repository)
  => Never forget to use @Transactional in service side as it needs commit on success, rollback on error
  => url : http://localhost:8080/api/employees/addEmployee
  => Since its PostMapping and adding new entry, HttpStatus should be 201 Created
  => It is recommended to show location as well in the ResponseEntity 
  
  **@PostMapping("/addEmployees") //http://localhost:8080/api/employees/addEmployees
      public ResponseEntity<List<EmployeeResponseDto>> addEmployees (@RequestBody List<EmployeeRequestDto> employeeRequestDtoList)**
  
  => It is used to add multiple Employees at a time
  => ModelMapper did not work, So used manual mapping. Manual mapping is more reliable than ModelMapper
  => It uses the default method saveAll(employeeList) (from JPARepository/CRUD repository)
  => Never forget to use @Transactional in service side as it needs commit on success, rollback on error, batch insert, all or nothing
  => url : http://localhost:8080/api/employees/addEmployees
  => Since its PostMapping and adding new entry, HttpStatus should be 201 Created
  => It is recommended to show location as well in the ResponseEntity
  
  **@GetMapping("/{id}") //http://localhost:8080/api/employees/{id}
      EmployeeResponseDto getEmployeeById(@PathVariable Long id)**
  
  => It uses PathVariable
  => sample url : http://localhost:8080/api/employees/{id} 
  => It uses the default method findById(id) (from JPARepository/CRUD repository)
  
  **@GetMapping
      public List<EmployeeResponseDto> getAllEmployees()**
  
  => sample url : http://localhost:8080/api/employees
  => It uses the default method findAll() (from JPARepository/CRUD repository)
  
  **@GetMapping("/searchEmployeeById") //http://localhost:8080/api/employees/searchEmployeeById?id={id}&name={name}
      EmployeeResponseDto searchEmployeeById(@Valid @RequestParam("id") Long id)**  
  
  => Sample url : http://localhost:8080/api/employees/searchEmployeeById?id=1
  => It uses RequestParam
  => It uses the default method findById(id) (from JPARepository/CRUD repository)
  
  **@GetMapping("/searchEmployees")
      public ResponseEntity<List<EmployeeResponseDto>> searchEmployees(@RequestParam(name="name", required = false) String employeeName,
                                                @RequestParam(name="department", required = false) String department,
                                                @RequestParam(name="minSalary", required = false) Integer minSalary)**
  
  => Sample url :
          //http://localhost:8080/api/employees/searchEmployees?name={name}
          //http://localhost:8080/api/employees/searchEmployees?department={department}
          //http://localhost:8080/api/employees/searchEmployees?minSalary={minSalary}
          //http://localhost:8080/api/employees/searchEmployees?department={department}&minSalary={minSalary}
          //http://localhost:8080/api/employees/searchEmployees
  => It uses RequestParam
  => It uses the default method findAll() (from JPARepository/CRUD repository), then applies required filters
  
  **@PutMapping("/updateEmployeeById/{id}")
      public ResponseEntity<EmployeeResponseDto> updateEmployeeById(@Valid @RequestBody EmployeeRequestDto employeeRequestDto, @PathVariable Long id)**
  
  => Sample url : http://localhost:8080/api/employees/updateEmployeeById/10
  => It uses PathVariable
  => It uses the default method findById(id), save(employee)
  => Since its PutMapping and updating the existing entry, HttpStatus should be 200 Ok 
  => Remember we need to throw ResourceNotFoundException if the given id does not exist
  => Since it is updating based on id (ie id is primary key, unique), It will update exactly one entry at a time
  
  **@PutMapping("/updateEmployeeByName/{name}")
      public ResponseEntity<List<EmployeeResponseDto>> updateEmployeeByName(@Valid @RequestBody EmployeeRequestDto employeeRequestDto, @PathVariable ("name") String name)**
  
  =>Sample url : http://localhost:8080/api/employees/updateEmployeeByName/Sachin 
  Since it is updating based on name (ie name is not primary key, it can be duplicate), It may update multiple entries at a time
  =>  Remember we need to throw ResourceNotFoundException if no record found for the given name
  => It uses the default method findByName(name)
  => For save, it uses save(employee) in loop
  => Since its PutMapping and updating the existing entry, HttpStatus should be 200 Ok 
  
  **@DeleteMapping("/deleteEmployeeById/{id}")
      public ResponseEntity<HttpStatus> deleteEmployeeById(@PathVariable("id") Long id)**
  
  => Sample url : http://localhost:8080/api/employees/deleteEmployeeById/4
  => Since it is deleting based on id (ie id is primary key, unique), It will delete exactly one entry at a time
  => Remember we need to throw ResourceNotFoundException if the given id does not exist
  => Since its DeleteMapping, HttpStatus should be 204 - No content
  
  **@GetMapping("/getAllEmployeesWithPagination")
      public Page<EmployeeResponseDto> getAllEmployeesWithPagination(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)**
  
  => Instead of List<EmployeeResponseDto>, it returns Page<EmployeeResponseDto> wise
  => Sample url : 
          http://localhost:8080/api/employees/getAllEmployeesWithPagination?page=0&size=5 
          http://localhost:8080/api/employees/getAllEmployeesWithPagination?page=1&size=5
          http://localhost:8080/api/employees/getAllEmployeesWithPagination?sort=salary,desc&size=10
          http://localhost:8080/api/employees/getAllEmployeesWithPagination?page=0&size=5&sort=salary,desc
  => If we do not mention query params in url, it will consider @PageableDefault values
  
   
  
  **@GetMapping("/searchEmployeesWithPagination")
      public Page<EmployeeResponseDto> searchEmployeesWithPagination(@RequestParam(name="name", required = false) String employeeName,
                                                                     @RequestParam(name="department", required=false) String department,
                                                                     @RequestParam(name="minSalary", required=false) Integer minSalary,
                                                                     Pageable pageable)**
  
  => Sample url : http://localhost:8080/api/employees/searchEmployeesWithPagination1?page=0&size=5&name=Sach
  => To enable pagination with search filters, we can use functional interface Specification
  => The below line is depricated,  but still we added for core understanding
  Specification<Employee> spec = Specification.where(null);
  => Actually, above line creates WHERE 1=1 SQL structure and so we can add chain conditions (AND/OR)
  => Remeber RQCB - Root, Query, CriteriaBuilder
  => We use employeeRepository.findAll(spec, pageable);
  => Note : We have to declare the method findAll(spec, pageable) in EmployeeRepository.java file
  => If we don't use @PageableDefault, page =0, size=20 by default      
  
  **@GetMapping("/searchEmployeesWithPagination1")
      public Page<EmployeeResponseDto> searchEmployeesWithPagination1(@RequestParam(name="name", required = false) String employeeName,
                                                                     @RequestParam(name="department", required=false) String department,
                                                                     @RequestParam(name="minSalary", required=false) Integer minSalary,
                                                                     Pageable pageable)**
  
  => This serves the same purpose of searchEmployeesWithPagination1
  => Since the below line is depricated, we used modern ways speciation.allOf(specs), Specification.anyOf(specs)
  Specification<Employee> spec = Specification.where(null);
  => Specification.allOf(specs) - Combines the filters as AND conditions in SQL
  => Specification.anyOf(specs) - Combines the filters as OR conditions in SQL

7. Sample payloads
   See samples/ folder in the repo for JSON examples
8. Actuator Endpoints (Monitoring)
   Health: http://localhost:8080/actuator/health
   Metrics: http://localhost:8080/actuator/metrics
   Info: http://localhost:8080/actuator/info
9. Testing
   Controller tests: @WebMvcTest + MockMvc
   Repository tests: @DataJpaTest
   Service tests: @SpringBootTest + @MockBean
   Run tests: Right-click test classes → Run
10.Author
   Mavishnu KJ
   LinkedIn : www.linkedin.com/in/mavishnu-kj
