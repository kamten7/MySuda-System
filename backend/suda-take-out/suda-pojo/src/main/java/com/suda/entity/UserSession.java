package com.suda.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户AI会话实体
 * 封装会话的所有信息：用户ID、会话ID、对话历史、时间戳
 * 便于Redis序列化和反序列化
 * 支持会话过期管理
 * 数据结构清晰，易于维护
 * 支持扩展（如添加会话状态、上下文信息等）
 * 统一管理会话生命周期
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession implements Serializable {

    /**
     * 序列化版本号
     * 作用：确保对象在不同版本的Java中保持一致的序列化格式
     */
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（UUID，全局唯一）
     * 作用：区分不同的会话周期
     */
    private String sessionId;

    /**
     * 用户ID（从JWT Token中获取）
     * 作用：确保会话归属正确，防止用户间数据混乱
     */
    private Long userId;

    /**
     * 对话历史记录
     * 作用：保持上下文连贯性，让AI理解前文
     */
    private List<Exchange> history;

    /**
     * 会话创建时间
     * 作用：用于统计和审计
     */
    private LocalDateTime createdAt;

    /**
     * 最后活跃时间
     * 作用：判断会话是否过期（超过2天未活跃）
     */
    private LocalDateTime lastActiveAt;

    /**
     * 一轮对话记录
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exchange implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 用户消息 */
        private String userMessage;

        /** AI回复 */
        private String assistantReply;

        /** 消息时间戳 */
        private LocalDateTime timestamp;
    }

    /**
     * 添加一轮对话到历史记录
     *
     * @param userMessage 用户消息
     * @param assistantReply AI回复
     * @param maxRounds 最大保留轮数
     */
    public void addExchange(String userMessage, String assistantReply, int maxRounds) {
        if (this.history == null) {
            this.history = new ArrayList<>();
        }

        // 添加新对话
        this.history.add(new Exchange(userMessage, assistantReply, LocalDateTime.now()));

        // 超过最大轮数时移除最早的对话
        while (this.history.size() > maxRounds) {
            this.history.remove(0);
        }

        // 更新最后活跃时间
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 检查会话是否过期
     *
     * @param expireDays 过期天数
     * @return true=已过期，false=未过期
     */
    public boolean isExpired(int expireDays) {
        if (lastActiveAt == null) {
            return true;
        }
        return lastActiveAt.plusDays(expireDays).isBefore(LocalDateTime.now());
    }
}