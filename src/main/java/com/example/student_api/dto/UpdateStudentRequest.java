package com.example.student_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// 更新成绩只需要邮箱和成绩，不要求姓名。
public record UpdateStudentRequest(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,
        @Min(value = 0, message = "成绩不能小于0")
        @Max(value = 100, message = "成绩不能大于100")
        double score) {
}
