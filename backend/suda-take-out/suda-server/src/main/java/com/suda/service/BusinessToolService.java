package com.suda.service;

import com.suda.vo.OrderReportVO;
import com.suda.vo.SalesTop10ReportVO;
import com.suda.vo.TurnoverReportVO;
import com.suda.vo.UserReportVO;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <h2>业务工具集</h2>
 * <p>这个类里每个方法都是一件"工具"——AI 会根据用户的问题自动挑选合适的工具来调用。</p>
 *
 * <h3>工作原理（用通俗的话说）</h3>
 * <ol>
 *   <li>用户问："最近 7 天哪些菜卖得最好？"</li>
 *   <li>AI 会识别出：需要"时间范围 = 最近7天" + "菜品销量排名"</li>
 *   <li>AI 自动调用 {@link #getSalesTop10}，传入它推导出的起止日期</li>
 *   <li>方法返回数据 → AI 收到数据 → AI 组织成自然语言回答用户</li>
 * </ol>
 *
 * <h3>@Tool 注解</h3>
 * <p>被 {@code @Tool} 标记的方法会自动暴露给 AI。
 * 注解里的字符串是给 AI 看的"说明书"——帮助 AI 判断什么时候该用这个工具。</p>
 *
 * <p><b>新增工具时只需照猫画虎：</b>加一个 public 方法 + @Tool("…描述…") 即可，无需改其他地方。</p>
 */
@Slf4j
@Component
public class BusinessToolService {

    private final ReportService reportService;

    public BusinessToolService(ReportService reportService) {
        this.reportService = reportService;
    }

    // ==================== 时间基准 ====================

    /**
     * <h3>获取当前日期</h3>
     * <p>大模型不知道"今天"是哪天（训练数据有截止日期），
     * 因此处理"最近7天""本周""本月"等时间表达前必须先调用此工具获取真实日期。</p>
     */
    @Tool("获取当前真实的日期（年月日）。在处理任何涉及时间的查询前，必须先调用此方法确定当前日期。返回格式：yyyy-MM-dd")
    public String getCurrentDate() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        log.info("AI调用工具 → 获取当前日期: {}", today);
        return today;
    }

    // ==================== 营业额 ====================

    @Tool("查询指定时间范围内的每日营业额（单位：元）。参数 beginDate 和 endDate 是必填的日期范围。")
    public TurnoverReportVO getTurnover(LocalDate beginDate, LocalDate endDate) {
        log.info("AI调用工具 → 营业额查询: {} ~ {}", beginDate, endDate);
        return reportService.getTurnover(beginDate, endDate);
    }

    // ==================== 订单统计 ====================

    @Tool("查询指定时间范围内的订单统计：总订单数、有效订单数、各状态订单数。")
    public OrderReportVO getOrderStatistics(LocalDate beginDate, LocalDate endDate) {
        log.info("AI调用工具 → 订单统计: {} ~ {}", beginDate, endDate);
        return reportService.getOrderStatistics(beginDate, endDate);
    }

    // ==================== 菜品销量 Top10 ====================

    @Tool("查询指定时间范围内销量最高的前 10 个菜品，返回菜品名称和销量。适合回答'什么卖得最好'类问题。")
    public SalesTop10ReportVO getSalesTop10(LocalDate beginDate, LocalDate endDate) {
        log.info("AI调用工具 → 菜品销量Top10: {} ~ {}", beginDate, endDate);
        return reportService.getSalesTop10(beginDate, endDate);
    }

    // ==================== 用户统计 ====================

    @Tool("查询指定时间范围内的用户统计：新增用户数、总用户数。")
    public UserReportVO getUserStatistics(LocalDate beginDate, LocalDate endDate) {
        log.info("AI调用工具 → 用户统计: {} ~ {}", beginDate, endDate);
        return reportService.getUserStatistics(beginDate, endDate);
    }
}
