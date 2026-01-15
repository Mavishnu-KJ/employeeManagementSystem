package com.example.employeeManagementSystem.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.autoconfigure.web.WebProperties;

@Entity
@Table(name="employees")
public class Employee {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Positive(message = "Salary must be positive")
    private Integer salary;

    @Email(message = "Invalid email")
    private String email;

    private String department;

    // Explicit no-arg constructor (JPA requirement)
    public Employee(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }
}
