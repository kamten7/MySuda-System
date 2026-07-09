package com.suda.service;

import com.suda.dto.AIQueryRequest;
import jakarta.servlet.AsyncContext;

/**
 * AI 服务
 */
public interface AIService {

    /** 自然语言查询 → 流式 SSE */
    void streamQuery(AIQueryRequest request, AsyncContext asyncContext);

    /** 一键诊断 → AI 自动拉取多维度数据生成经营报告 */
    void diagnose(AsyncContext asyncContext);
}
