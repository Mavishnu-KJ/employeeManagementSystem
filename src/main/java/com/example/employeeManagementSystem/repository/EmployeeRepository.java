package com.example.employeeManagementSystem.repository;

import com.example.employeeManagementSystem.model.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByNameContaining(String name);
    List<Employee> findBySalaryGreaterThan(Integer salary);
    List<Employee> findByIdAndName(Long id, String name);
    List<Employee> findByName(String name);
    List<Employee> findByNameLike(String name);
    Boolean existsByEmail(String email);
    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);
    @Query("SELECT e.email from Employee e WHERE e.email IN :emailList") //In JPQL (and HQL), you do not use the database table name (employees) in the FROM clause. You must use the entity class name (Employee).
    List<String> findEmailsIn(@Param("emailList") List<String> emailList);
}

