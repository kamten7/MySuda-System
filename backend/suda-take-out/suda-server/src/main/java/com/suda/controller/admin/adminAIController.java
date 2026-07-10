package com.suda.controller.admin;

import com.suda.dto.AIQueryRequest;
import com.suda.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 智能查询 + 运营诊断
 */
@Slf4j
@RestController
@RequestMapping("/admin/ai")
@Tag(name = "AI智能查询")
public class AdminAIController {

    private final AIService aiService;

    public AdminAIController(AIService aiService) {
        this.aiService = aiService;
    }

    /** 自然语言查询 → 流式 SSE */
    @PostMapping("/query/stream")
    @Operation(summary = "AI智能查询（流式输出）")
    public void streamQuery(@RequestBody AIQueryRequest request,
                            HttpServletRequest httpRequest,
                            HttpServletResponse httpResponse) {
        log.info(": {}", request.getQuestion());
        httpRequest.startAsync().setTimeout(120_000L);
        aiService.streamQuery(request, httpRequest.getAsyncContext());
    }

    /** 一键诊断 → AI 自动拉取多维度数据生成经营报告 */
    @PostMapping("/diagnose")
    @Operation(summary = "AI一键经营诊断")
    public void diagnose(HttpServletRequest httpRequest,
                         HttpServletResponse httpResponse) {
        log.info("收到AI诊断请求");
        httpRequest.startAsync().setTimeout(180_000L); // 诊断更耗时
        aiService.diagnose(httpRequest.getAsyncContext());
    }
}
