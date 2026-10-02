package com.example.student_api.common;

//泛型类,听着挺高级的一东西,T是占位符;
public class Result<T>{
    private int code;
    private String message;

    //类型由使用者定;
    private T data;


    public Result(int code,String message,T data){
        this.code=code;
        this.message=message;
        this.data=data;
    }

    public static <T> Result<T> success(T data){
        return new Result<>(10000,"成功",data);
    }

    public static <T> Result<T> error(int code,String message){
        return new Result<>(code,message,null);
    }


    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }

    public T getData(){
        return data;
    }
}
