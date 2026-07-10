package com.suda.service.impl;

import com.suda.AI.tools.CartTools;
import com.suda.dto.UserAIRequest;
import com.suda.service.ShoppingCartService;
import com.suda.service.UserAIService;
import com.suda.vo.UserAIResponse;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户端 AI 点餐助手实现
 *
 * <p>核心设计：通过 System Prompt 严格约束 AI 行为，确保购物车操作必须经用户明确确认。</p>
 *
 * <h3>对话记忆</h3>
 * 使用 ConcurrentHashMap 按 sessionId 存储最近 N 轮对话，每次请求时拼入上下文。
 */
@Service
@Slf4j
public class UserAIServiceImpl implements UserAIService {

    private final ChatClient chatClient;
    private final CartTools cartTools;
    private final ShoppingCartService shoppingCartService;

    /** 按 sessionId 存储的对话历史，每个 session 保留最近 {@value #MAX_HISTORY_ROUNDS} 轮 */
    private final ConcurrentHashMap<String, List<Exchange>> conversationStore = new ConcurrentHashMap<>();

    private static final int MAX_HISTORY_ROUNDS = 10;

    /**
     * 系统提示词 —— 引导 AI 遵循"推荐 → 询问 → 确认 → 操作"的交互流程。
     */
    private static final String SYSTEM_PROMPT = """
            你是速达外卖的智能点餐助手，名叫"小速"。你的职责是帮助用户发现美食并完成点餐。

            ## 核心规则（必须严格遵守）

            ### 规则1：购物车操作必须用户明确确认
            - 你**绝对不能**在用户没有明确要求的情况下调用 addToCart、removeFromCart、clearCart
            - "明确要求"的示例：
              "加入购物车"、"帮我加"、"帮我选"、"下单"、"来一份"、"就这个"、"好的就它了"、
              "帮我点"、"加到购物车里"、"给我来一份"、"我要这个"、"行就这个吧"、"可以帮我加了"
            - 以下情况**不算**明确要求（此时只能查询/推荐，禁止操作购物车）：
              "我想吃辣的"、"有什么推荐的"、"今天吃什么好"、"有什么好吃的"、
              "帮我看看"、"有没有便宜的"、"推荐一下"
            - 当用户只是咨询时，你只能调用 searchDishes / getRecommendations / getHotDishes / getDishDetail / getCategories

            ### 规则2：推荐后必须主动询问
            - 当用户表达想吃某类食物但未明确要求加购时，先查询/推荐菜品
            - 推荐完成后，**必须**主动问用户："需要我帮您把某某菜品加入购物车吗？"
            - 只有用户明确回复确认（"好的"、"可以"、"行"、"嗯"、"加吧"）后，才能调用 addToCart

            ### 规则3：查看购物车是安全的
            - getCart 可以随时调用，不受上述限制
            - 当用户问"我的购物车有什么"时直接调用即可

            ## 你的工具
            1. searchDishes       — 搜索菜品（按关键词、分类、口味）
            2. getRecommendations — 智能推荐菜品
            3. getHotDishes       — 热门菜品排行
            4. getDishDetail      — 查看菜品详情
            5. getCategories      — 查看菜品分类
            6. getBudgetFriendlyDishes — 实惠菜品推荐
            7. addToCart          — 加入购物车 ⚠️ 需用户确认
            8. getCart            — 查看购物车
            9. removeFromCart     — 移出购物车 ⚠️ 需用户确认
            10. clearCart         — 清空购物车 ⚠️ 需用户确认

            ## 回复风格
            - 语气亲切自然，适当使用 emoji
            - 推荐菜品时说明推荐理由
            - 回复简洁，控制在 200 字以内
            """;

    public UserAIServiceImpl(ChatClient chatClient,
                             CartTools cartTools,
                             ShoppingCartService shoppingCartService) {
        this.chatClient = chatClient;
        this.cartTools = cartTools;
        this.shoppingCartService = shoppingCartService;
    }

    // ==================== 对话记忆管理 ====================

    /**
     * 加载会话对话历史，返回格式化后的上下文字符串。
     */
    private String buildHistoryContext(String sessionId) {
        List<Exchange> history = conversationStore.get(sessionId);
        if (history == null || history.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("## 历史对话回顾\n");
        for (Exchange e : history) {
            sb.append("用户：").append(e.userMessage).append("\n");
            sb.append("小速：").append(e.assistantReply).append("\n");
        }
        sb.append("---\n");
        return sb.toString();
    }

    /**
     * 保存一轮对话到指定会话。
     */
    private void saveExchange(String sessionId, String userMessage, String assistantReply) {
        conversationStore.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(
                new Exchange(userMessage, assistantReply));

        // 超过最大轮数时移除最早的
        List<Exchange> history = conversationStore.get(sessionId);
        while (history.size() > MAX_HISTORY_ROUNDS) {
            history.remove(0);
        }
    }

    // ==================== 同步聊天 ====================

    @Override
    public UserAIResponse chat(UserAIRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
        String userMessage = request.getMessage();

        log.info("用户AI聊天 [session={}]：{}", sessionId, userMessage);

        // 1. 构建上下文
        String historyContext = buildHistoryContext(sessionId);
        String fullUserPrompt = historyContext.isEmpty()
                ? userMessage
                : historyContext + "\n当前用户消息：" + userMessage;

        // 2. 调用 AI（工具已通过 ChatClient.defaultTools 注册，无需每次传入）
        String response;
        try {
            response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(fullUserPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            response = "抱歉，我暂时无法处理您的请求，请稍后再试 😅";
        }

        log.info("AI 回复 [session={}]：{}", sessionId,
                response != null ? response.substring(0, Math.min(100, response.length())) + "..." : "null");

        if (response == null) {
            response = "抱歉，我没有理解您的问题，可以换个方式再问一次吗？";
        }

        // 3. 保存对话历史
        saveExchange(sessionId, userMessage, response);

        // 4. 检测是否更新了购物车
        boolean cartUpdated = detectCartOperation(response);

        return UserAIResponse.builder()
                .content(response)
                .cartUpdated(cartUpdated)
                .cartCount(getCartItemCount())
                .sessionId(sessionId)
                .build();
    }

    // ==================== 流式聊天 ====================

    @Override
    public void streamChat(UserAIRequest request, AsyncContext asyncContext) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
        String userMessage = request.getMessage();

        log.info("用户AI流式聊天 [session={}]：{}", sessionId, userMessage);

        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        // 构建上下文
        String historyContext = buildHistoryContext(sessionId);
        String fullUserPrompt = historyContext.isEmpty()
                ? userMessage
                : historyContext + "\n当前用户消息：" + userMessage;

        // 收集完整回复以存入历史
        StringBuilder fullResponse = new StringBuilder();

        try {
            PrintWriter writer = response.getWriter();

            chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(fullUserPrompt)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        fullResponse.append(chunk);
                        writeSSE(writer, "data: " + chunk);
                    })
                    .doOnComplete(() -> {
                        writeSSE(writer, "data: [DONE]");
                        writer.flush();
                        writer.close();

                        // 保存对话历史（使用收集的完整回复）
                        saveExchange(sessionId, userMessage, fullResponse.toString());

                        asyncContext.complete();
                        log.info("流式聊天完成 [session={}]，回复长度：{}", sessionId, fullResponse.length());
                    })
                    .doOnError(error -> {
                        log.error("流式响应错误", error);
                        try {
                            writeSSE(writer, "data: [ERROR] 抱歉，服务暂时不可用");
                            writeSSE(writer, "data: [DONE]");
                            writer.flush();
                            writer.close();
                        } catch (Exception ignored) {
                        }
                        asyncContext.complete();
                    })
                    .subscribe();

        } catch (IOException e) {
            log.error("流式响应IO异常", e);
            asyncContext.complete();
        }
    }

    // ==================== 购物车状态 ====================

    @Override
    public String getCartStatus() {
        return cartTools.getCart();
    }

    // ==================== 历史管理 ====================

    @Override
    public void clearHistory(String sessionId) {
        conversationStore.remove(sessionId);
        log.info("已清空对话历史：{}", sessionId);
    }

    // ==================== 内部方法 ====================

    /**
     * 检测 AI 回复是否包含购物车操作。
     * 通过匹配 CartTools 方法的返回值来判定。
     */
    private boolean detectCartOperation(String response) {
        if (response == null) return false;
        return response.contains("已添加到购物车")
                || response.contains("已从购物车移除")
                || response.contains("购物车已清空");
    }

    private int getCartItemCount() {
        try {
            List<?> cart = shoppingCartService.showShoppingCart();
            return cart != null ? cart.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void writeSSE(PrintWriter writer, String data) {
        writer.write(data + "\n\n");
        writer.flush();
    }

    // ==================== 内部数据结构 ====================

    /**
     * 一轮对话交换记录。
     */
    private record Exchange(String userMessage, String assistantReply) {
    }
}
