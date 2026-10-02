package com.example.student_api.handler;


import com.example.student_api.common.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DuplicateKeyException;

@RestControllerAdvice //全局“异常拦截器";
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(org.springframework.dao.DuplicateKeyException e){
        return Result.error(10001,"邮箱已存在");
    }

    //顶级糊弄兜底打法;
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        return Result.error(99999,"系统繁忙,请稍后再试");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e){
        //这老长一段,写的啥啊;
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Result.error(10002,message);
    }
}
