package com.suda.enumeration;

import org.springframework.stereotype.Component;

/**
 * 数据库操作类型枚举类
 * 用于AOP拦截器进行数据库操作记录
 */
public enum OperationType {

    /**
     * 更新操作
     */
    UPDATE,

    /**
     * 插入操作
     */
    INSERT

}
