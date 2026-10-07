package com.example.student_api.exception;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException() {
        super("学生不存在");
    }
}
