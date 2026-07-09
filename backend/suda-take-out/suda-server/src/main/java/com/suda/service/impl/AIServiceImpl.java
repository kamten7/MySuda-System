package com.suda.service.impl;

import com.suda.AI.AIAgent.DiagnosisAssistant;
import com.suda.AI.AIAgent.StreamQueryAssistant;
import com.suda.dto.AIQueryRequest;
import com.suda.service.AIService;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;

/**
 * AI 流式查询 + 运营诊断
 */
@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private final StreamQueryAssistant streamQueryAssistant;
    private final DiagnosisAssistant diagnosisAssistant;

    public AIServiceImpl(StreamQueryAssistant streamQueryAssistant,
                         DiagnosisAssistant diagnosisAssistant) {
        this.streamQueryAssistant = streamQueryAssistant;
        this.diagnosisAssistant = diagnosisAssistant;
    }

    // ==================== 自然语言查询 ====================

    @Override
    public void streamQuery(AIQueryRequest request, AsyncContext asyncContext) {
        log.info("AI流式查询 → {}", request.getQuestion());

        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        setupSSE(response);

        try {
            PrintWriter writer = response.getWriter();
            streamQueryAssistant.chat(request.getQuestion())
                    .onPartialResponse(token -> writeToken(writer, token))
                    .onCompleteResponse(resp -> finish(writer, asyncContext))
                    .onError(error -> fail(writer, asyncContext, error))
                    .start();
        } catch (Exception e) {
            log.error("AI查询启动失败", e);
            asyncContext.complete();
        }
    }

    // ==================== 一键诊断 ====================

    @Override
    public void diagnose(AsyncContext asyncContext) {
        log.info("AI诊断 → 生成经营报告");

        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        setupSSE(response);

        try {
            PrintWriter writer = response.getWriter();
            diagnosisAssistant.diagnose()
                    .onPartialResponse(token -> writeToken(writer, token))
                    .onCompleteResponse(resp -> finish(writer, asyncContext))
                    .onError(error -> fail(writer, asyncContext, error))
                    .start();
        } catch (Exception e) {
            log.error("AI诊断启动失败", e);
            asyncContext.complete();
        }
    }

    // ==================== 私有工具方法 ====================

    private void setupSSE(HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
    }

    private void writeToken(PrintWriter writer, String token) {
        writer.write("data:" + token + "\n\n");
        writer.flush();
    }

    private void finish(PrintWriter writer, AsyncContext ctx) {
        writer.write("data:[DONE]\n\n");
        writer.flush();
        writer.close();
        ctx.complete();
        log.info("AI流式输出完成");
    }

    private void fail(PrintWriter writer, AsyncContext ctx, Throwable error) {
        log.error("AI输出失败: {}", error.getMessage());
        writer.write("data:ERROR:" + error.getMessage() + "\n\n");
        writer.flush();
        writer.close();
        ctx.complete();
    }
}
