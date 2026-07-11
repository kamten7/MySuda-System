package com.suda.AI;

import com.suda.service.BusinessToolService;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * <h2>AI 总配置</h2>
 * <p>创建流式聊天模型 + 绑定业务工具的 AI 助手 Bean。</p>
 *
 * <h3>调用链路</h3>
 * <pre>
 * Controller → AIService → StreamQueryAssistant.chat(问题)
 *   → AI 自动判定 → 调 @Tool 查数据库 → 流式输出回答
 * </pre>
 */
@Configuration
public class AIAgent {

    @Value("${suda.ai.deepseek.api-key}")
    private String apiKey;

    @Value("${suda.ai.deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${suda.ai.deepseek.model-name:deepseek-v4-flash}")
    private String modelName;

    // ==================== 流式模型 ====================

    @Bean
    public OpenAiStreamingChatModel openAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.1)       // 0=最确定，避免 AI 自由发挥
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    // ==================== 流式助手接口 ====================

    public interface StreamQueryAssistant {

        @SystemMessage("""
            你是速达外卖的数据库查询AI。

            【最重要的规则——每次对话必须遵守】
            你的训练数据有截止日期，你不知道"今天"是哪一天。
            因此：处理任何涉及时（今天、本周、本月、最近N天、半年等）的查询时，
            第一步必须先调用 getCurrentDate() 获取真实日期，然后根据真实日期计算时间范围。

            【其他规则】
            - 拿到工具返回的数据后，用中文口语概括，不要输出JSON
            - 数值保留1-2位小数，加上单位（元、人、单等）
            - 如果查不到数据，如实说"目前该时间段暂无数据"
            """)
        @UserMessage("{{userMessage}}")
        TokenStream chat(@V("userMessage") String userMessage);
    }

    // ==================== 查询助手 ====================

    @Bean
    public StreamQueryAssistant streamQueryAssistant(
            OpenAiStreamingChatModel streamingChatModel,
            BusinessToolService businessToolService) {
        return AiServices.builder(StreamQueryAssistant.class)
                .streamingChatModel(streamingChatModel)
                .tools(businessToolService)
                .build();
    }

    // ==================== 诊断助手（一键运营分析） ====================

    /**
     * 诊断助手 — 与查询助手共用同一套 @Tool，但 SystemMessage 不同：
     * 查询助手按用户问题逐一回答，诊断助手一次性拉取多维度数据并输出报告
     */
    public interface DiagnosisAssistant {

        @SystemMessage("""
            你是速达外卖的运营诊断AI。你会收到一条指令，要求你生成一份完整的经营诊断报告。

            【第一步——确定时间】
            先调用 getCurrentDate() 获取真实日期。

            【第二步——拉取数据（多轮工具调用）】
            依次调用以下工具，获取最近7天的数据：
            - getTurnover(7天前, 今天)    → 营业额明细
            - getOrderStatistics(7天前, 今天) → 订单统计
            - getSalesTop10(7天前, 今天)  → 热销菜品
            - getUserStatistics(7天前, 今天) → 用户统计

            如果你发现最近7天没数据，尝试扩大到最近30天。

            【第三步——生成报告】
            收到所有数据后，用结构化格式输出诊断报告：

            📊 **速达外卖经营诊断报告**（日期范围）

            **一、营业额概览**
            - 7天总营业额：xx 元
            - 日均：xx 元
            - 最高日：日期（xx元） / 最低日：日期（xx元）

            **二、订单分析**
            - 总订单数 / 有效订单数 / 完成率
            - 各状态分布

            **三、热销菜品 TOP5**
            列出名称+销量

            **四、用户数据**
            - 新增用户 / 总用户

            **五、经营建议**
            根据数据给出2-3条具体可执行的经营建议（如：哪几天需要加推活动、哪些菜可以加大推广等）

            报告用口语化中文呈现，适当使用emoji增强可读性。
            """)
        @UserMessage("请生成一份完整的经营诊断报告。")
        TokenStream diagnose();
    }

    @Bean
    public DiagnosisAssistant diagnosisAssistant(
            OpenAiStreamingChatModel streamingChatModel,
            BusinessToolService businessToolService) {
        return AiServices.builder(DiagnosisAssistant.class)
                .streamingChatModel(streamingChatModel)
                .tools(businessToolService)
                .build();
    }
}
