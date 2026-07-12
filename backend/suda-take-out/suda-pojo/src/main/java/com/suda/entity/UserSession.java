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
     * 上一轮推荐的菜品列表（结构化存储，用于"需要"等确认意图时定位菜品）
     * 每次 AI 调用 searchDishes / getRecommendations / getHotDishes 等推荐工具后更新
     */
    private List<RecommendedDish> lastRecommendedDishes;

    /**
     * 推荐菜品快照 — 记录 ID、名称、价格，供确认加购时精准定位
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedDish implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long dishId;
        private Long setmealId;
        private String name;
        private Double price;
    }

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

    /**
     * 更新上一轮推荐的菜品列表（供后续"需要"等确认意图精准定位）
     */
    public void updateRecommendedDishes(List<RecommendedDish> dishes) {
        this.lastRecommendedDishes = dishes;
    }

    /**
     * 构建推荐菜品的结构化提示文本，注入到用户消息上下文中
     */
    public String buildRecommendationContext() {
        if (lastRecommendedDishes == null || lastRecommendedDishes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("\n## 上一轮推荐的菜品（结构化上下文）\n");
        sb.append("用户说'需要''好的''加吧'等确认词时，你必须调用 addToCart 函数，传入以下菜品ID：\n");
        for (RecommendedDish d : lastRecommendedDishes) {
            if (d.getDishId() != null) {
                sb.append(String.format("- 菜品ID=%d, 名称='%s', 价格=¥%.2f\n",
                        d.getDishId(), d.getName(), d.getPrice()));
            } else if (d.getSetmealId() != null) {
                sb.append(String.format("- 套餐ID=%d, 名称='%s', 价格=¥%.2f\n",
                        d.getSetmealId(), d.getName(), d.getPrice()));
            }
        }
        return sb.toString();
    }

    /**
     * 从会话历史中反向查找最后一次包含 [ID:xxx] 标记的 AI 回复，解析出推荐菜品。
     * 这是兜底加购的最可靠数据源——持久化在 Redis 中的历史对话不会被跨线程丢失。
     *
     * @return 解析出的推荐菜品列表，可能为空
     */
    public List<RecommendedDish> findLastRecommendedFromHistory() {
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }

        // 从最新到最旧遍历历史记录
        for (int i = history.size() - 1; i >= 0; i--) {
            Exchange exchange = history.get(i);
            if (exchange.getAssistantReply() == null) continue;

            List<RecommendedDish> dishes =
                    parseDishIdsFromText(exchange.getAssistantReply());
            if (!dishes.isEmpty()) {
                return dishes;
            }
        }
        return new ArrayList<>();
    }

    /**
     * 从文本中解析 [ID:xxx] 格式的菜品信息（内联实现，避免跨模块依赖）。
     */
    private static List<RecommendedDish> parseDishIdsFromText(String text) {
        List<RecommendedDish> dishes = new ArrayList<>();
        if (text == null || text.isBlank()) return dishes;

        java.util.regex.Pattern idPattern = java.util.regex.Pattern.compile("\\[ID:(\\d+)\\]");
        java.util.regex.Pattern pricePattern = java.util.regex.Pattern.compile("¥(\\d+\\.?\\d*)");

        for (String line : text.split("\n")) {
            java.util.regex.Matcher m = idPattern.matcher(line);
            if (!m.find()) continue;

            Long dishId = Long.parseLong(m.group(1));
            // 提取名称
            String cleaned = line.replaceFirst("^[\\d]+\\.[\\s]*", "")
                                .replaceFirst("^[-•][\\s]*", "");
            int priceIdx = cleaned.indexOf('¥');
            int dashIdx = cleaned.indexOf('—');
            int bracketIdx = cleaned.indexOf('[');
            int endIdx = cleaned.length();
            if (priceIdx > 0) endIdx = Math.min(endIdx, priceIdx);
            if (dashIdx > 0) endIdx = Math.min(endIdx, dashIdx);
            if (bracketIdx > 0) endIdx = Math.min(endIdx, bracketIdx);
            String name = cleaned.substring(0, endIdx).trim();
            // 提取价格
            Double price = null;
            java.util.regex.Matcher pm = pricePattern.matcher(line);
            if (pm.find()) {
                try { price = Double.parseDouble(pm.group(1)); } catch (NumberFormatException ignored) {}
            }
            dishes.add(new RecommendedDish(dishId, null, name, price));
        }
        return dishes;
    }
}