package com.suda.AI.service;

import com.suda.entity.UserSession;
import org.springframework.stereotype.Service;

/**
 * 根据意图类型和预执行结果构建增强 Prompt。
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li>System Prompt 极简——只告诉 AI 它是谁、怎么回复</li>
 *   <li>User Prompt 包含真实数据——AI 只需用自然语言"翻译"数据，不用自己查</li>
 *   <li>数据中的 [ID:xxx] 标记用于后续加购追踪</li>
 * </ul>
 */
@Service
public class UserAIPromptBuilder {

    private static final String SYSTEM_PROMPT = """
            你是速达外卖的智能点餐助手'小速'。

            ## 规则
            1. 使用下面「系统查到的菜品数据」回复用户，不要编造菜品信息
            2. 菜品列表格式：每道菜独占一行，用数字编号开头，如"1. 珍珠奶茶 ¥10.00（人气推荐）"
            3. 禁止输出任何 ID、编号标记或内部标识符
            4. 推荐完成后主动问："需要帮您加入购物车吗？"
            5. 如果数据为空，如实告诉用户并建议换个关键词
            6. 语气亲切自然，适当使用 emoji，回复控制在 150 字以内
            """;

    private static final String SYSTEM_PROMPT_CHAT = """
            你是速达外卖的智能点餐助手'小速'。用亲切自然的语气回复用户的问候或问题，
            适当使用 emoji。回复控制在 100 字以内。
            """;

    private static final String SYSTEM_PROMPT_CART = """
            你是速达外卖的智能点餐助手'小速'。下面是购物车操作的结果。
            请用亲切自然的语气告知用户操作结果，适当使用 emoji。回复控制在 80 字以内。
            """;

    /**
     * 构建完整的 Prompt 对象。
     */
    public Prompt build(UserAIIntentService.IntentResult intentResult,
                        UserSession session) {

        String systemPrompt;
        String userPrompt;

        switch (intentResult.intent()) {
            case ADD_TO_CART, VIEW_CART, CLEAR_CART -> {
                systemPrompt = SYSTEM_PROMPT_CART;
                userPrompt = "## 购物车操作结果\n" + intentResult.toolOutput()
                        + "\n\n请告知用户操作结果。";
            }
            case RECOMMEND, SEARCH -> {
                systemPrompt = SYSTEM_PROMPT;
                userPrompt = buildDataPrompt(intentResult.toolOutput(), session);
            }
            default -> {
                systemPrompt = SYSTEM_PROMPT_CHAT;
                userPrompt = buildChatPrompt(intentResult.toolOutput(), session);
            }
        }

        return new Prompt(systemPrompt, userPrompt);
    }

    /**
     * 构建包含用户历史 + 结构化数据的 Prompt。
     */
    private String buildDataPrompt(String toolOutput, UserSession session) {
        StringBuilder sb = new StringBuilder();

        // 历史上下文
        String history = buildHistoryContext(session);
        if (!history.isEmpty()) {
            sb.append(history).append("\n");
        }

        sb.append("## 系统查到的菜品数据\n");
        sb.append(toolOutput != null ? toolOutput : "暂无数据");
        sb.append("\n\n请根据以上数据回复用户。");

        return sb.toString();
    }

    /**
     * 构建闲聊 Prompt（无工具数据）。
     */
    private String buildChatPrompt(String toolOutput, UserSession session) {
        StringBuilder sb = new StringBuilder();

        String history = buildHistoryContext(session);
        if (!history.isEmpty()) {
            sb.append(history).append("\n");
        }

        sb.append("## 用户消息\n");
        sb.append(toolOutput != null ? toolOutput : "（无）");

        return sb.toString();
    }

    /**
     * 构建历史对话上下文（最近 5 轮）。
     */
    private String buildHistoryContext(UserSession session) {
        if (session == null || session.getHistory() == null || session.getHistory().isEmpty()) {
            return "";
        }

        // 取最近 5 轮
        int size = session.getHistory().size();
        int start = Math.max(0, size - 5);
        var recent = session.getHistory().subList(start, size);

        StringBuilder sb = new StringBuilder("## 历史对话\n");
        for (var e : recent) {
            sb.append("用户：").append(e.getUserMessage()).append("\n");
            sb.append("小速：").append(e.getAssistantReply()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Prompt 值对象。
     */
    public record Prompt(String system, String user) {
    }
}
