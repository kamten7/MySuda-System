package com.suda.controller.user;


import com.suda.dto.UserAIRequest;
import com.suda.result.Result;
import com.suda.service.UserAIService;
import com.suda.vo.UserAIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/ai")
@Slf4j
@Tag(name = "用户AI接口")
public class UserAIController {


    private final UserAIService userAIService;

    public UserAIController(UserAIService userAIService) {
        this.userAIService = userAIService;
    }


    /**
     * 用户查询推荐菜品，基于自然语言对话查，不进行agent工具调用
     * 仅仅是对话机器人
     * 同步响应
     * @param request 用户输入的消息和会话ID
     * @return 包含AI回答、购物车更新状态、购物车数量、会话ID的响应
     */
    @PostMapping("/chat")
    @Operation(summary = "AI聊天（同步响应）")
    public Result<UserAIResponse> chat(@RequestBody UserAIRequest request) {
        log.info("用户AI聊天输入：{}", request.getMessage());
        UserAIResponse response = userAIService.chat(request);
        log.info("用户AI聊天输出：{}", response.getContent());
        return Result.success(response);
    }

    /**
     * 用户查询推荐菜品，基于自然语言对话查，不进行agent工具调用
     * 仅仅是对话机器人
     * 流式响应
     * @param request
     * @param httpRequest
     * @param httpResponse
     */
    @PostMapping("/chat/stream")
    @Operation(summary = "AI聊天（流式响应）")
    public void streamChat(@RequestBody UserAIRequest request,
                           HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse) {
        log.info("用户AI流式聊天：{}", request.getMessage());
        httpRequest.startAsync().setTimeout(60_000L);
        userAIService.streamChat(request, httpRequest.getAsyncContext());
    }


    /**
     * 用户需要帮忙下单，基于自然语言对话查，进行agent工具调用
     * 添加适合用户的菜品到购物车
     * 用户自行选择下单
     */
    @GetMapping("/cart/status")
    @Operation(summary = "获取购物车状态")
    public Result<String> getCartStatus() {
        String status = userAIService.getCartStatus();
        return Result.success(status);
    }

    /**
     * 清空用户对话历史
     * @param sessionId 会话ID
     * @return 无
     */
    @DeleteMapping("/history/{sessionId}")
    @Operation(summary = "清空对话历史")
    public Result<Void> clearHistory(@PathVariable String sessionId) {
        log.info("清空对话历史：{}", sessionId);
        userAIService.clearHistory(sessionId);
        return Result.success();
    }
}
