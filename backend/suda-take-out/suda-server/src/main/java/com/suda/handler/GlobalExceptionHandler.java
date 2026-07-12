package com.suda.handler;

import com.suda.constant.MessageConstant;
import com.suda.exception.BaseException;
import com.suda.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * SQL异常处理
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error("异常信息：{}", ex.getMessage());
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username=split[2];
            String msg=username+ MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    /**
     * 兜底：捕获所有未被处理的异常，返回 JSON 而非 HTML 500
     * 防止 Spring AI / Redis / 第三方 API 调用异常时返回 HTML 错误页
     */
    @ExceptionHandler
    public Result exceptionHandler(Exception ex) {
        log.error("未知异常", ex);
        return Result.error(ex.getMessage() != null ? ex.getMessage() : "服务器内部错误");
    }
}
