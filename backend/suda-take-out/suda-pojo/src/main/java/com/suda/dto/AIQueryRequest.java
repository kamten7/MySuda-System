package com.suda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * AI 智能查询请求
 * <p>前端把用户输入的自然语言问题发过来，后端交给大模型处理。</p>
 *
 * <pre>
 * 示例：
 * { "question": "上周营业额是多少？" }
 * { "question": "本月新增了多少用户？" }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQueryRequest {

    /** 用户的自然语言问题（必填），例如 "最近7天哪些菜卖得最好？" */
    private String question;

    /** 可选：开始日期。用户没指定时 AI 自己根据"最近7天""上周"等说法推导 */
    private LocalDate beginDate;

    /** 可选：结束日期。用户没指定时 AI 自己推导 */
    private LocalDate endDate;
}
