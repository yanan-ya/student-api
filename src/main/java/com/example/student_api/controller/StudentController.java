package com.example.student_api.controller;

import com.example.student_api.entity.Student;
import com.example.student_api.dto.UpdateStudentRequest;
import com.example.student_api.service.StudentService;
import com.example.student_api.common.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService){
        this.studentService=studentService;
    }
    @GetMapping("/student")
    public Result<List<Student>> getStudent(){
        return Result.success(studentService.getAllStudent());//数据包进信封;
    }

    //统一信封;
    @PutMapping("/student")//put 更新;
    public Result<Void> updateStudent(@Valid @RequestBody UpdateStudentRequest request){
        studentService.updateScore(request.email(),request.score());
        return Result.success(null);
    }

    @DeleteMapping("/student")
    public Result<Void> deleteStudent(
            @RequestParam @NotBlank(message = "邮箱不能为空")
            @Email(message = "邮箱格式不正确") String email){
        studentService.deleteByEmail(email);
        return Result.success(null);
    }

    @PostMapping("/student")
    //@Valid 是进门之前,先按标签检查一遍;
    public Result<Void> addStudent(@Valid @RequestBody Student student){
        studentService.addStudent(student.getName(),student.getEmail(),student.getScore());
        return Result.success(null);
    }
}
