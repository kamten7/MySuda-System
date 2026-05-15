package com.suda.aspect;

import com.suda.annotation.AutoFill;
import com.suda.constant.AutoFillConstant;
import com.suda.context.BaseContext;
import com.suda.enumeration.OperationType;
import io.lettuce.core.dynamic.support.ReflectionUtils;
import javassist.bytecode.SignatureAttribute;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段填充功能
 */

@Aspect//声明这是一个切面类
@Component// 让 Spring 管理这个切面类
@Slf4j
public class AutiFillAspect {
    /**
     * 切入点
     * 对那些类的那些方法进行拦截
     * 指定拦截的方法：对所有mapper包下的所有类的所有方法进行拦截（这些方法需要加上@AutoFill注解）
     */
    @Pointcut("execution(* com.suda.mapper.*.*(..)) && @annotation(com.suda.annotation.AutoFill)")
    public void autoFillPointCut(){}
    /**
     * 前置通知
     * 在通知中进行公共字段的赋值
     * 在方法执行前进行公共字段填充处
     * @param joinPoint 连接点对象，包含方法执行前后的信息
     *
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段填充");
        //获取当前被拦截的方法的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();
        //获取当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            log.info("参数为空，无需进行公共字段填充");
            return;
        }
        //获取实体对象
        Object entity = (Object) args[0];
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if (operationType == OperationType.INSERT) {
            //对其进行反射赋值
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //反射赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {

            }
        } else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //反射赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("当前方法没有更新时间或更新人字段");
                e.printStackTrace();
            }

        }
    }
}























