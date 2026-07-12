package com.suda.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    private Long dishId;      // 菜品id
    private Long setmealId;   // 套餐id
    private String dishFlavor; // 菜品的口味
    private Long userId;      // 用户id（跨线程兜底：AICart 调用发生在 reactor 线程，BaseContext ThreadLocal 不可用）

}
