package com.example.employeeManagementSystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Email already exists")


public class DuplicateEmailException extends RuntimeException{

    public DuplicateEmailException(){
        super("Email already exists");
    }

    public DuplicateEmailException(String email){
        super("Email already exists: " + email);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }

}
