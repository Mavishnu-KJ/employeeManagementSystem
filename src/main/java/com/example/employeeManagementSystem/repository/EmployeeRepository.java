package com.example.employeeManagementSystem.repository;

import com.example.employeeManagementSystem.model.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByNameContaining(String name);
    List<Employee> findBySalaryGreaterThan(Integer salary);
    List<Employee> findByIdAndName(Long id, String name);
    List<Employee> findByName(String name);

    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);
}

