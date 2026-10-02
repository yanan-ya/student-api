package com.example.student_api.controller;

import com.example.student_api.entity.Student;
import com.example.student_api.service.StudentService;
import com.example.student_api.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService){
        this.studentService=studentService;
    }
    @GetMapping("/student")
    public Result<List<Student>> getStudent() throws Exception{
        return Result.success(studentService.getAllStudent());//数据包进信封;
    }

    //统一信封;
    @PutMapping("/student")//put 更新;
    public Result<Void> updateStudent(@RequestBody Student student){
        int rows = studentService.updateScore(student.getEmail(),student.getScore());
        return Result.success(null);
    }

    @DeleteMapping("/student")
    public Result<Void> deleteStudent(@RequestParam String email){//这也是新注释,从URL的？email=xxx里取值;
        int rows = studentService.deleteByEmail(email);
        return Result.success(null);
    }

    @PostMapping("/student")
    //@Valid 是进门之前,先按标签检查一遍;
    public Result<Void> addStudent(@Valid @RequestBody Student student){
        studentService.addStudent(student.getName(),student.getEmail(),student.getScore());
        return Result.success(null);
    }
}