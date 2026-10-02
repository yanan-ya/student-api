package com.example.student_api.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class Student {
    private Integer id;//允许存在没有值
    //声明检查标签;
    @NotBlank(message="名字不能为空")//message是给用户看的话;
    private String name;

    @Min(value=0,message="成绩不能小于0")
    @Max(value=100,message="成绩不能大于100")
    private double score;

    @NotBlank(message="邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    public Student(Integer id,String name,double score,String email){
        this.id=id;
        this.name=name;
        this.email=email;
        this.score=score;
    }

    public Student() {

    }

    public void setId(Integer id){
        this.id=id;
    }

    public Integer getId(){
        return id;
    }


    public void setName(String name){
        this.name=name;
    }

    public String getName(){
        return name;
    }


    public void setEmail(String email){
        this.email=email;
    }

    public String getEmail(){
        return email;
    }
    public void setScore(double score){
        this.score=score;
    }

    public double getScore(){
        return score;
    }
}
