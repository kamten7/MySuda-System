package com.suda.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// 用户AI响应
/**
 * <h2>用户AI响应</h2>
 * <p>包含AI回答、购物车更新状态、购物车数量、会话ID。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAIResponse {
    private String content;// AI回答
    private Boolean cartUpdated;// 购物车是否更新
    private Integer cartCount;// 购物车数量
    private String sessionId;// 会话ID
}