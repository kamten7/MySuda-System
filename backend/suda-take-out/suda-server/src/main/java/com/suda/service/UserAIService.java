package com.suda.service;

import com.suda.dto.UserAIRequest;
import com.suda.vo.UserAIResponse;
import jakarta.servlet.AsyncContext;

public interface UserAIService {

    /**
     * 处理用户AI请求，返回AI回答。
     * @param request 用户输入的消息和会话ID
     * @return 包含AI回答、购物车更新状态、购物车数量、会话ID的响应
     */
    UserAIResponse chat(UserAIRequest request);

    /**
     * 处理用户AI请求，返回流式AI回答。
     * @param request 用户输入的消息和会话ID
     * @param asyncContext 异步上下文，用于流式输出
     */
    void streamChat(UserAIRequest request, AsyncContext asyncContext);

    /**
     * 获取用户购物车状态。
     * @return 购物车状态描述
     */
    String getCartStatus();

    /**
     * 清空指定会话的对话历史。
     * @param sessionId 会话ID
     */
    void clearHistory(String sessionId);
}