package com.example.student_api.handler;


import com.example.student_api.common.Result;
import com.example.student_api.exception.StudentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.dao.DuplicateKeyException;

@RestControllerAdvice //全局“异常拦截器";
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e){
        return Result.error(10001,"邮箱已存在");
    }

    @ExceptionHandler(StudentNotFoundException.class)
    public Result<Void> handleStudentNotFound(StudentNotFoundException e){
        return Result.error(10003,e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            HandlerMethodValidationException.class})
    public Result<Void> handleInvalidRequest(Exception e){
        return Result.error(10002,"请求参数不正确");
    }

    //客户端收到通用提示，服务端保留异常堆栈以便排查。
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        log.error("处理请求时发生未预期异常", e);
        return Result.error(99999,"系统繁忙,请稍后再试");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse("请求参数不正确");
        return Result.error(10002,message);
    }
}
