package com.suda.annotation;

import com.suda.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，实现自动填充功能
 * 用于标识某个方法需要进行功能字段填充处理
 */
//指定方法级别注解
@Target(ElementType.METHOD)//指定在方法上使用
@Retention(RetentionPolicy.RUNTIME)//保留到运行时
public @interface AutoFill {
    //数据库操作类型，UPDATE INSERT
    OperationType value();// 指定操作类型：INSERT 或 UPDATE

}
