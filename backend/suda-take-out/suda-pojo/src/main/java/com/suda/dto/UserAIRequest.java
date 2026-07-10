package com.suda.dto;


import lombok.Data;

@Data
public class UserAIRequest {
    // 用户输入的消息
    private String message;

    // 会话ID
    private String sessionId;
}
