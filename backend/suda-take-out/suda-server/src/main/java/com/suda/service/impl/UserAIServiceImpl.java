package com.suda.service.impl;

import com.suda.AI.service.UserAIIntentService;
import com.suda.AI.service.UserAIIntentService.IntentResult;
import com.suda.AI.service.UserAIPromptBuilder;
import com.suda.AI.service.UserAIPromptBuilder.Prompt;
import com.suda.AI.tools.CartTools;
import com.suda.context.BaseContext;
import com.suda.dto.UserAIRequest;
import com.suda.entity.UserSession;
import com.suda.entity.UserSession.RecommendedDish;
import com.suda.service.ShoppingCartService;
import com.suda.service.UserAIService;
import com.suda.vo.UserAIResponse;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户端 AI 点餐助手实现（v3.0 预执行架构）。
 *
 * <h3>v3.0 预执行架构</h3>
 * <ol>
 *   <li>Java 意图检测 + 工具预执行（确定性）</li>
 *   <li>LangChain4j 流式文本生成（纯 NLG）</li>
 *   <li>Java 后处理（会话持久化 + 加购事件）</li>
 * </ol>
 */
@Service
@Slf4j
public class UserAIServiceImpl implements UserAIService {

    private final OpenAiStreamingChatModel streamingChatModel;
    private final UserAIIntentService intentService;
    private final UserAIPromptBuilder promptBuilder;
    private final CartTools cartTools;
    private final ShoppingCartService shoppingCartService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_KEY_PREFIX = "ai:session:user:";
    private static final long SESSION_TTL_SECONDS = 2 * 24 * 3600;
    private static final int MAX_HISTORY_ROUNDS = 50;

    public UserAIServiceImpl(
            @Qualifier("userStreamingChatModel") OpenAiStreamingChatModel streamingChatModel,
            UserAIIntentService intentService,
            UserAIPromptBuilder promptBuilder,
            CartTools cartTools,
            ShoppingCartService shoppingCartService) {
        this.streamingChatModel = streamingChatModel;
        this.intentService = intentService;
        this.promptBuilder = promptBuilder;
        this.cartTools = cartTools;
        this.shoppingCartService = shoppingCartService;
    }

    // ==================== 同步聊天 ====================

    @Override
    public UserAIResponse chat(UserAIRequest request) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return UserAIResponse.builder()
                    .content("抱歉，请先登录后再使用AI助手 😅").sessionId(null).build();
        }

        UserSession session = getOrCreateSession(userId);
        String userMessage = request.getMessage();

        log.info("AI同步聊天 [userId={}, sessionId={}]: {}", userId, session.getSessionId(), userMessage);

        // 1. 意图检测 + 工具预执行
        List<RecommendedDish> prevRecommended = session.getLastRecommendedDishes();
        cartTools.setCurrentUserId(userId);
        IntentResult intentResult = intentService.detectAndExecute(userMessage, prevRecommended, userId);

        // 2. 构建 Prompt
        Prompt prompt = promptBuilder.build(intentResult, session);

        // 3. 收集流式输出为完整响应
        StringBuilder fullResponse = new StringBuilder();
        try {
            streamingChatModel.chat(
                    List.of(SystemMessage.from(prompt.system()), UserMessage.from(prompt.user())),
                    new StreamingChatResponseHandler() {
                        @Override
                        public void onPartialResponse(String token) { fullResponse.append(token); }
                        @Override
                        public void onCompleteResponse(ChatResponse response) { /* done */ }
                        @Override
                        public void onError(Throwable error) {
                            log.error("AI同步请求失败: {}", error.getMessage());
                        }
                    });
            // 等待流式完成（LangChain4j OpenAiStreamingChatModel 内部使用 OkHttp 同步流式）
            Thread.sleep(100); // 给流式一点时间开始
            int waitCount = 0;
            while (fullResponse.isEmpty() && waitCount < 600) { // 最多等 60 秒
                Thread.sleep(100);
                waitCount++;
            }
            Thread.sleep(500); // 等待最后几个 token
        } catch (Exception e) {
            log.error("AI同步调用失败", e);
            if (fullResponse.isEmpty()) {
                fullResponse.append("抱歉，我暂时无法处理您的请求，请稍后再试 😅");
            }
        }

        String aiResponse = fullResponse.toString();
        log.info("AI同步回复 [userId={}]: {}", userId,
                aiResponse.length() > 80 ? aiResponse.substring(0, 80) + "..." : aiResponse);

        // 4. 后处理
        String finalResponse = postProcess(aiResponse, intentResult, session);

        // 5. 保存会话
        session.addExchange(userMessage, finalResponse, MAX_HISTORY_ROUNDS);
        saveSession(session);

        boolean cartUpdated = intentResult.cartModified()
                || finalResponse.contains("已添加到购物车")
                || finalResponse.contains("已从购物车移除");

        return UserAIResponse.builder()
                .content(finalResponse)
                .cartUpdated(cartUpdated)
                .cartCount(getCartItemCount())
                .sessionId(session.getSessionId())
                .build();
    }

    // ==================== 流式聊天 ====================

    @Override
    public void streamChat(UserAIRequest request, AsyncContext asyncContext) {
        Long userId = BaseContext.getCurrentId();
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        if (userId == null) {
            writeSSEAndClose(response, "data: 抱歉，请先登录后再使用AI助手 😅\n\n", "data: [DONE]\n\n");
            asyncContext.complete();
            return;
        }

        UserSession session = getOrCreateSession(userId);
        String userMessage = request.getMessage();

        log.info("AI流式聊天 [userId={}, sessionId={}]: {}",
                userId, session.getSessionId(), userMessage);

        try {
            PrintWriter writer = response.getWriter();

            // 1. 意图检测 + 工具预执行
            List<RecommendedDish> prevRecommended = session.getLastRecommendedDishes();
            cartTools.setCurrentUserId(userId);
            IntentResult intentResult = intentService.detectAndExecute(userMessage, prevRecommended, userId);

            log.info("意图检测结果: intent={}, cartModified={}, dishes={}",
                    intentResult.intent(), intentResult.cartModified(),
                    intentResult.dishes() != null ? intentResult.dishes().size() : 0);

            // 如果意图不需要 AI 生成文本（如纯购物车操作），直接构造回复
            if (shouldSkipAI(intentResult)) {
                String directResponse = buildDirectResponse(intentResult);
                writeSSE(writer, directResponse);
                writeSSE(writer, "[DONE]");
                if (intentResult.cartModified()) {
                    writeSSE(writer, "[CART_UPDATED:" + getCartItemCount() + "]");
                }
                writer.flush(); writer.close();
                session.addExchange(userMessage, directResponse, MAX_HISTORY_ROUNDS);
                postProcess(directResponse, intentResult, session);
                saveSession(session);
                asyncContext.complete();
                return;
            }

            // 2. 构建 Prompt
            Prompt prompt = promptBuilder.build(intentResult, session);

            // 3. 流式生成
            StringBuilder fullResponse = new StringBuilder();

            streamingChatModel.chat(
                    List.of(SystemMessage.from(prompt.system()), UserMessage.from(prompt.user())),
                    new StreamingChatResponseHandler() {
                        @Override
                        public void onPartialResponse(String chunk) {
                            // 前端安全网：去除 AI 可能输出的所有 ID 标记变体
                            String clean = com.suda.AI.tools.RecommendationContextHolder.stripMarkers(chunk);
                            fullResponse.append(clean);
                            writeSSE(writer, clean);
                        }
                        @Override
                        public void onCompleteResponse(ChatResponse resp) {
                            String aiText = fullResponse.toString();
                            // 4. 后处理
                            String finalResponse = postProcess(aiText, intentResult, session);

                            // 发送购物车更新
                            if (intentResult.cartModified()) {
                                writeSSE(writer, "[CART_UPDATED:" + getCartItemCount() + "]");
                            }

                            writeSSE(writer, "[DONE]");
                            writer.flush(); writer.close();

                            // 5. 保存会话
                            session.addExchange(userMessage, finalResponse, MAX_HISTORY_ROUNDS);
                            saveSession(session);

                            log.info("流式聊天完成 [userId={}]，回复长度：{}", userId, finalResponse.length());
                            asyncContext.complete();
                        }
                        @Override
                        public void onError(Throwable error) {
                            log.error("AI流式调用失败", error);
                            try {
                                writeSSE(writer, "[ERROR] 抱歉，服务暂时不可用");
                                writeSSE(writer, "[DONE]");
                                writer.flush(); writer.close();
                            } catch (Exception ignored) {}
                            asyncContext.complete();
                        }
                    });

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

    @Override
    public void clearHistory(String sessionId) {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            try { redisTemplate.delete(SESSION_KEY_PREFIX + userId); }
            catch (Exception e) { log.warn("Redis删除会话失败: {}", SESSION_KEY_PREFIX + userId, e); }
            log.info("已清空对话历史 [userId={}, sessionId={}]", userId, sessionId);
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 判断是否跳过 AI 文本生成（纯操作类意图）。
     */
    private boolean shouldSkipAI(IntentResult intentResult) {
        return switch (intentResult.intent()) {
            case ADD_TO_CART, VIEW_CART, CLEAR_CART -> true;
            default -> false;
        };
    }

    /**
     * 对于购物车操作类意图，直接构造回复文本，不需要 AI 生成。
     */
    private String buildDirectResponse(IntentResult intentResult) {
        return switch (intentResult.intent()) {
            case ADD_TO_CART -> intentResult.toolOutput()
                    + "\n\n需要继续点餐的话，随时告诉我哦~";
            case VIEW_CART -> intentResult.toolOutput();
            case CLEAR_CART -> "✅ " + intentResult.toolOutput();
            default -> intentResult.toolOutput() != null ? intentResult.toolOutput() : "好的~";
        };
    }

    /**
     * AI 回复后处理：持久化推荐上下文。
     */
    private String postProcess(String aiResponse, IntentResult intentResult, UserSession session) {
        // 持久化推荐菜品（来自工具预执行阶段）
        if (intentResult.dishes() != null && !intentResult.dishes().isEmpty()) {
            session.updateRecommendedDishes(intentResult.dishes());
            log.info("推荐上下文已持久化: {} 道菜品", intentResult.dishes().size());
        } else {
            // 尝试从 AI 文本中解析（兜底）
            List<RecommendedDish> fromText =
                    com.suda.AI.tools.RecommendationContextHolder.parseFromToolResult(aiResponse);
            if (!fromText.isEmpty()) {
                session.updateRecommendedDishes(fromText);
                log.info("推荐上下文从AI文本解析: {} 道菜品", fromText.size());
            }
        }

        return aiResponse;
    }

    private UserSession getOrCreateSession(Long userId) {
        String key = SESSION_KEY_PREFIX + userId;
        UserSession session = null;
        try { session = (UserSession) redisTemplate.opsForValue().get(key); }
        catch (Exception e) { log.warn("Redis读取会话失败: key={}", key, e); }

        if (session == null || session.isExpired(2)) {
            session = UserSession.builder()
                    .sessionId(UUID.randomUUID().toString())
                    .userId(userId)
                    .history(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .lastActiveAt(LocalDateTime.now())
                    .build();
            log.info("创建新会话 [userId={}, sessionId={}]", userId, session.getSessionId());
        }
        return session;
    }

    private void saveSession(UserSession session) {
        session.setLastActiveAt(LocalDateTime.now());
        try {
            redisTemplate.opsForValue().set(
                    SESSION_KEY_PREFIX + session.getUserId(), session,
                    SESSION_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis保存会话失败: key={}", SESSION_KEY_PREFIX + session.getUserId(), e);
        }
    }

    private int getCartItemCount() {
        try {
            List<?> cart = shoppingCartService.showShoppingCart();
            return cart != null ? cart.size() : 0;
        } catch (Exception e) { return 0; }
    }

    /** 写入 SSE 数据。自动将多行内容拆分为多个 data: 行。 */
    private void writeSSE(PrintWriter writer, String data) {
        // SSE 规范要求每行以 "data:" 开头。若内容含 \n，需要每行单独加前缀
        for (String line : data.split("\n", -1)) {
            writer.write("data:" + line + "\n");
        }
        writer.write("\n"); // 空行表示事件结束
        writer.flush();
    }

    /** 写入单个 SSE 事件（不含换行的内容）。 */
    private void writeSSELine(PrintWriter writer, String data) {
        writer.write("data:" + data + "\n\n");
        writer.flush();
    }

    private void writeSSEAndClose(HttpServletResponse response, String... lines) {
        try {
            PrintWriter w = response.getWriter();
            for (String line : lines) w.write(line);
            w.flush(); w.close();
        } catch (IOException ignored) {}
    }
}
