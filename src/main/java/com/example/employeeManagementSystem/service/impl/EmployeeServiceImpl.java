package com.example.employeeManagementSystem.service.impl;

import com.example.employeeManagementSystem.exception.ResourceNotFoundException;
import com.example.employeeManagementSystem.model.dto.EmployeeRequestDto;
import com.example.employeeManagementSystem.model.dto.EmployeeResponseDto;
import com.example.employeeManagementSystem.repository.EmployeeRepository;
import com.example.employeeManagementSystem.model.entity.Employee;
import com.example.employeeManagementSystem.service.EmployeeService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final ModelMapper mapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, ModelMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EmployeeResponseDto addEmployee(EmployeeRequestDto employeeRequestDto){
        logger.info("addEmployee, employeeRequestDto is {}", employeeRequestDto);
        //Employee employee = mapper.map(employeeRequestDto, Employee.class); //ModelMapper did not work

        Employee employee = mapEmployeeRequestDtoToEmployeeEntity(employeeRequestDto);

        logger.debug("addEmployee, employee is {}", employee);
        Employee saved = employeeRepository.save(employee);
        logger.info("addEmployee, saved is {}", saved);
        return mapper.map(saved, EmployeeResponseDto.class);
    }

    @Override
    @Transactional
    public List<EmployeeResponseDto> addEmployees(List<EmployeeRequestDto> employeeRequestDtoList){
        logger.info("addEmployees, employeeRequestDtoList is {}", employeeRequestDtoList);
        List<EmployeeResponseDto> employeeResponseDtoList = new ArrayList<>();
        List<Employee> employeeList;

        if(employeeRequestDtoList == null || employeeRequestDtoList.isEmpty()){
            throw new ResourceNotFoundException("No Employees data found to add");
        }

        employeeList = employeeRequestDtoList.stream().map(this::mapEmployeeRequestDtoToEmployeeEntity).toList();

        /*
        for(EmployeeRequestDto employeeRequestDto : employeeRequestDtoList){
            employeeList.add(mapEmployeeRequestDtoToEmployeeEntity(employeeRequestDto));
        }
        */

        logger.debug("addEmployees, employeeList is {}", employeeList);

        //saveAll
        List<Employee> employeeListAdded = employeeRepository.saveAll(employeeList);

        //map EmployeeEntity To EmployeeResponseDto
        if(employeeListAdded !=null && !employeeListAdded.isEmpty()) {
            employeeResponseDtoList = employeeListAdded.stream().map(emp -> mapEmployeeEntityToEmployeeResponseDto(emp)).toList();
        }

        /*
        //Map to ResponseDto
        if(employeeListAdded !=null && !employeeListAdded.isEmpty()){
            for(Employee emp : employeeListAdded){
                employeeResponseDtoList.add(mapEmployeeEntityToEmployeeResponseDto(emp));
            }
        }
        */

        logger.info("addEmployees, employeeListAdded is {}", employeeListAdded);
        return employeeResponseDtoList;
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Employee not found with id: " + id));
        return mapper.map(employee, EmployeeResponseDto.class);
    }

    @Override
    public List<EmployeeResponseDto> getAllEmployees(){
        return employeeRepository.findAll().stream()
                .map(employee->mapper.map(employee,EmployeeResponseDto.class))
                .toList();
    }

    @Override
    public EmployeeResponseDto searchEmployeeById(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Employee not found with id: " + id));
        return mapper.map(employee, EmployeeResponseDto.class);
    }

    @Override
    public List<EmployeeResponseDto> searchEmployees(String name, String department, Integer minSalary){

        //Get All employees
        List<Employee> employeeList = employeeRepository.findAll();

        //Create employees stream - useful for small dataset
        Stream<Employee> employeesStream = employeeList.stream();

        //Apply dynamic filters
        if(name !=null && !name.isBlank()){
            var lowerCaseName = name.toLowerCase();
            employeesStream = employeesStream.filter(emp->emp.getName().toLowerCase().contains(lowerCaseName));
        }

        if(department !=null && !department.isBlank()){
            employeesStream = employeesStream.filter(emp->emp.getDepartment().equalsIgnoreCase(department));
        }

        if(minSalary != null){
            employeesStream = employeesStream.filter(emp->emp.getSalary() >= minSalary);
        }

        //Map to EmployeeResponseDto and then to List
        return employeesStream.map(this::mapEmployeeEntityToEmployeeResponseDto).toList();

    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployeeById(EmployeeRequestDto employeeRequestDto, Long id){
        logger.info("updateEmployeeById, employeeRequestDto is {} and id is {}", employeeRequestDto, id);

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Resource not found for the given id: "+id));

        logger.debug("updateEmployeeById, existingEmployee is {}", existingEmployee);

        existingEmployee.setName(employeeRequestDto.name());
        existingEmployee.setSalary(employeeRequestDto.salary());
        existingEmployee.setDepartment(employeeRequestDto.department());
        existingEmployee.setEmail(employeeRequestDto.email());

        logger.debug("updateEmployeeById, existingEmployee after change is {}", existingEmployee);

        Employee updated = employeeRepository.save(existingEmployee);
        logger.info("updateEmployeeById, updated is {}", updated);
        return mapper.map(updated, EmployeeResponseDto.class);

    }

    @Override
    @Transactional
    public List<EmployeeResponseDto> updateEmployeeByName(EmployeeRequestDto employeeRequestDto, String name) {
        logger.info("updateEmployeeByName, employeeRequestDto is {} and name is {}", employeeRequestDto, name);
        List<Employee> existingEmployeeList = employeeRepository.findByName(name);

        if(existingEmployeeList.isEmpty()){
            throw new ResourceNotFoundException("Resource not found for the name : "+name);
        }

        logger.debug("updateEmployeeByName, existingEmployeeList is {}", existingEmployeeList);

        List<EmployeeResponseDto> employeeResponseDtoList = new ArrayList<>();

        for(Employee existingEmployee : existingEmployeeList){
            existingEmployee.setSalary(employeeRequestDto.salary());
            existingEmployee.setDepartment(employeeRequestDto.department());
            existingEmployee.setEmail(employeeRequestDto.email());
            existingEmployee.setName(employeeRequestDto.name());

            //save
            Employee saved = employeeRepository.save(existingEmployee);

            logger.info("updateEmployeeByName, existingEmployee {} is updated", saved);

            //map to dto
            employeeResponseDtoList.add(mapEmployeeEntityToEmployeeResponseDto(saved));

        }

        logger.info("updateEmployeeByName, updated Employees List is {} ", employeeResponseDtoList);

        return employeeResponseDtoList;

    }

    public EmployeeResponseDto mapEmployeeEntityToEmployeeResponseDto(Employee employee){
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
        return employee;
    }



    @Override
    @Transactional
    public void deleteEmployeeById(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Resource not found for the Id: "+id));

        //Delete
        employeeRepository.delete(employee);

    }

    @Override
    public Page<EmployeeResponseDto> getAllEmployeesWithPagination(Pageable pageable){
        Page<Employee> employeeList = employeeRepository.findAll(pageable);

        //map to ResponseDTO
        return employeeList.map(emp-> mapEmployeeEntityToEmployeeResponseDto(emp)); // emp->mapEmployeeEntityToEmployeeResponseDto(emp) can be this::mapEmployeeEntityToEmployeeResponseDto
    }

    @Override
    public Page<EmployeeResponseDto> searchEmployeesWithPagination(String name, String department, Integer minSalary, Pageable pageable){

        Specification<Employee> spec = Specification.where(null); //depricated

        if(name!=null && !name.isBlank()){
            spec = spec.and((root, query, cb)->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
            );
        }

        if(department!=null && !department.isBlank()){
            spec = spec.and((root, query, cb)->
                    cb.equal(root.get("department"), department)
            );
        }

        if(minSalary != null){
            spec = spec.and((root, query, cb)->
                    cb.greaterThanOrEqualTo(root.get("minSalary"), minSalary)
            );
        }

        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);

        //map to EmployeeResponseDto
        return employeePage.map(this::mapEmployeeEntityToEmployeeResponseDto);

    }

}
