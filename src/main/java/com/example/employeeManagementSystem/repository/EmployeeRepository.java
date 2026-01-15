package com.example.employeeManagementSystem.repository;

import com.example.employeeManagementSystem.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByNameContaining(String name);
    List<Employee> findBySalaryGreaterThan(Integer salary);
    List<Employee> findByIdAndName(Long id, String name);
}

