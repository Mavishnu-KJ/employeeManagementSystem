package com.example.employeeManagementSystem.exception;

public class DuplicateEmailException extends RuntimeException{

    public DuplicateEmailException(){
        super("Duplicate Email ID found");
    }

    public DuplicateEmailException(String message){
        super(message);
    }

}
