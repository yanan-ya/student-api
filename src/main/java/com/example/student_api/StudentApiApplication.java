package com.example.student_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudentApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentApiApplication.class, args);
	}

}
/*
$body = @{
    name = "tom"
    email = "ceshi2@test.com"
    score = 80
} | ConvertTo-Json

$utf8Body = [System.Text.Encoding]::UTF8.GetBytes($body)

Invoke-RestMethod `
    -Uri "http://localhost:8080/student" `
    -Method Post `
    -ContentType "application/json; charset=utf-8" `
    -Body $utf8Body
 */
