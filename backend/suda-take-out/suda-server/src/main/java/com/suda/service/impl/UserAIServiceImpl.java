package com.suda.service.impl;

import com.suda.AI.tools.CartTools;
import com.suda.AI.tools.UserAITools;
import com.suda.context.BaseContext;
import com.suda.dto.UserAIRequest;
import com.suda.entity.UserSession;
import com.suda.service.ShoppingCartService;
import com.suda.service.UserAIService;
import com.suda.vo.UserAIResponse;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户端 AI 点餐助手实现
 * 基于用户ID的会话管理：确保不同用户的会话隔离
 * Redis持久化存储：服务重启后会话不丢失
 * 会话过期机制：2天未活跃自动清理
 * 时间工具集成：AI能理解时间上下文
 *
 */
@Service
@Slf4j
public class UserAIServiceImpl implements UserAIService {

    private final ChatClient chatClient;
    private final CartTools cartTools;
    private final ShoppingCartService shoppingCartService;
    private final UserAITools userAITools;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** Redis Key前缀 */
    private static final String SESSION_KEY_PREFIX = "ai:session:user:";

    /** 会话过期时间：2天（单位：秒） */
    private static final long SESSION_TTL_SECONDS = 2 * 24 * 3600;

    /** 最大保留对话轮数 */
    private static final int MAX_HISTORY_ROUNDS = 50;

    /**
     * 系统提示词 —— 引导 AI 遵循"推荐 → 询问 → 确认 → 操作"的交互流程
     * 新增：引导AI使用时间工具
     */
    private static final String SYSTEM_PROMPT = """
            你是速达外卖的智能点餐助手，名叫"小速"。你的职责是帮助用户发现美食并完成点餐。

            ## 核心规则（必须严格遵守）

            ### 规则0：时间感知（重要）
            - 你的训练数据有截止日期，你不知道"今天"是哪一天
            - 当用户询问"今天"、"现在"、"最近"等时间相关问题时，先调用 getCurrentDate 或 getTimeContext 获取真实时间
            - 根据当前时段智能推荐：早餐时间推荐早餐，午餐时间推荐午餐，以此类推
            - 示例：用户问"今天吃什么"，你应该先获取当前时间，然后根据时段推荐适合当前时间段的菜品

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
            1. getCurrentDate      — 获取当前日期（处理时间相关问题时必须先调用）
            2. getCurrentDateTime  — 获取当前日期和时间
            3. getTimeContext      — 获取时间上下文（星期几、时段、是否是用餐时间）
            4. searchDishes       — 搜索菜品（按关键词、分类、口味）
            5. getRecommendations — 智能推荐菜品
            6. getHotDishes       — 热门菜品排行
            7. getDishDetail      — 查看菜品详情
            8. getCategories      — 查看菜品分类
            9. getBudgetFriendlyDishes — 实惠菜品推荐
            10. addToCart          — 加入购物车 ⚠️ 需用户确认
            11. getCart            — 查看购物车
            12. removeFromCart     — 移出购物车 ⚠️ 需用户确认
            13. clearCart         — 清空购物车 ⚠️ 需用户确认

            ## 回复风格
            - 语气亲切自然，适当使用 emoji 表情
            - 推荐菜品时说明推荐理由
            - 回复简洁，控制在 100 字以内
            """;

    //构造器注入依赖
    public UserAIServiceImpl(ChatClient chatClient,
                             CartTools cartTools,
                             ShoppingCartService shoppingCartService,
                             UserAITools userAITools) {
        this.chatClient = chatClient;
        this.cartTools = cartTools;
        this.shoppingCartService = shoppingCartService;
        this.userAITools = userAITools;
    }

    // ==================== 会话管理 ====================

    /**
     * 获取或创建用户会话
     *
     * <h3>为什么这样设计？</h3>
     * <ul>
     *   <li>从BaseContext获取当前用户ID：确保会话归属正确</li>
     *   <li>Redis存储：支持分布式部署，服务重启不丢失</li>
     *   <li>自动过期：2天未活跃自动清理，节省存储空间</li>
     *   <li>UUID会话ID：防止会话ID冲突</li>
     * </ul>
     *
     * @param userId 用户ID
     * @return 用户会话
     */
    private UserSession getOrCreateSession(Long userId) {
        String sessionKey = SESSION_KEY_PREFIX + userId;

        // 从Redis获取会话
        UserSession session = (UserSession) redisTemplate.opsForValue().get(sessionKey);

        // 会话不存在或已过期，创建新会话
        if (session == null || session.isExpired(2)) {
            session = UserSession.builder()
                    .sessionId(UUID.randomUUID().toString())
                    .userId(userId)
                    .history(new java.util.ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .lastActiveAt(LocalDateTime.now())
                    .build();

            log.info("创建新会话 [userId={}, sessionId={}]", userId, session.getSessionId());
        }

        return session;
    }

    /**
     * 保存会话到Redis
     *
     * @param session 用户会话
     */
    private void saveSession(UserSession session) {
        String sessionKey = SESSION_KEY_PREFIX + session.getUserId();

        // 更新最后活跃时间
        session.setLastActiveAt(LocalDateTime.now());

        // 保存到Redis，设置2天过期时间
        redisTemplate.opsForValue().set(sessionKey, session, SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        log.debug("保存会话 [userId={}, sessionId={}, historySize={}]",
                session.getUserId(), session.getSessionId(), session.getHistory().size());
    }

    /**
     * 构建历史对话上下文
     */
    private String buildHistoryContext(UserSession session) {
        if (session.getHistory() == null || session.getHistory().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("## 历史对话回顾\n");
        for (UserSession.Exchange e : session.getHistory()) {
            sb.append("用户：").append(e.getUserMessage()).append("\n");
            sb.append("小速：").append(e.getAssistantReply()).append("\n");
        }
        sb.append("---\n");
        return sb.toString();
    }

    // ==================== 同步聊天 ====================

    @Override
    public UserAIResponse chat(UserAIRequest request) {
        // 从ThreadLocal获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            log.error("无法获取用户ID，可能未登录");
            return UserAIResponse.builder()
                    .content("抱歉，请先登录后再使用AI助手 😅")
                    .sessionId(null)
                    .build();
        }

        // 获取或创建会话
        UserSession session = getOrCreateSession(userId);
        String userMessage = request.getMessage();

        log.info("用户AI聊天 [userId={}, sessionId={}]：{}", userId, session.getSessionId(), userMessage);

        // 1. 构建上下文
        String historyContext = buildHistoryContext(session);
        String fullUserPrompt = historyContext.isEmpty()
                ? userMessage
                : historyContext + "\n当前用户消息：" + userMessage;

        // 2. 调用 AI
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

        log.info("AI 回复 [userId={}, sessionId={}]：{}", userId, session.getSessionId(),
                response != null ? response.substring(0, Math.min(100, response.length())) + "..." : "null");

        if (response == null) {
            response = "抱歉，我没有理解您的问题，可以换个方式再问一次吗？";
        }

        // 3. 保存对话历史
        session.addExchange(userMessage, response, MAX_HISTORY_ROUNDS);

        // 4. 保存会话到Redis
        saveSession(session);

        // 5. 检测是否更新了购物车
        boolean cartUpdated = detectCartOperation(response);

        return UserAIResponse.builder()
                .content(response)
                .cartUpdated(cartUpdated)
                .cartCount(getCartItemCount())
                .sessionId(session.getSessionId())
                .build();
    }

    // ==================== 流式聊天 ====================

    @Override
    public void streamChat(UserAIRequest request, AsyncContext asyncContext) {
        // 从ThreadLocal获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            log.error("无法获取用户ID，可能未登录");
            try {
                HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                PrintWriter writer = response.getWriter();
                writer.write("data: 抱歉，请先登录后再使用AI助手 😅\n\n");
                writer.write("data: [DONE]\n\n");
                writer.flush();
                writer.close();
                asyncContext.complete();
            } catch (IOException e) {
                log.error("流式响应IO异常", e);
            }
            return;
        }

        // 获取或创建会话
        UserSession session = getOrCreateSession(userId);
        String userMessage = request.getMessage();

        log.info("用户AI流式聊天 [userId={}, sessionId={}]：{}", userId, session.getSessionId(), userMessage);

        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        // 构建上下文
        String historyContext = buildHistoryContext(session);
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

                        // 保存对话历史
                        session.addExchange(userMessage, fullResponse.toString(), MAX_HISTORY_ROUNDS);

                        // 保存会话到Redis
                        saveSession(session);

                        asyncContext.complete();
                        log.info("流式聊天完成 [userId={}, sessionId={}]，回复长度：{}",
                                userId, session.getSessionId(), fullResponse.length());
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
        // 注意：这里应该根据用户ID清除，而不是sessionId
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            String sessionKey = SESSION_KEY_PREFIX + userId;
            redisTemplate.delete(sessionKey);
            log.info("已清空对话历史 [userId={}, sessionId={}]", userId, sessionId);
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 检测 AI 回复是否包含购物车操作
     */
    private boolean detectCartOperation(String response) {
        if (response == null) return false;
        return response.contains("已添加到购物车")
                || response.contains("已从购物车移除")
                || response.contains("购物车已清空");
    }

    private int getCartItemCount() {
        try {
            java.util.List<?> cart = shoppingCartService.showShoppingCart();
            return cart != null ? cart.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void writeSSE(PrintWriter writer, String data) {
        writer.write(data + "\n\n");
        writer.flush();
    }
}