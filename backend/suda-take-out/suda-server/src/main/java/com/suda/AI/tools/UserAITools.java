package com.suda.AI.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * 用户端AI时间工具
 * 大模型的训练数据有截止日期，不知道"今天"是哪一天
 * 用户可能问"今天有什么推荐"、"现在几点了"等问题
 * AI需要理解时间上下文（早餐、午餐、晚餐时段）
 * 让AI能够准确理解时间相关的用户请求
 * 提供更智能的推荐（如早餐时段推荐早餐）
 * 增强用户体验，对话更自然
 */
@Slf4j
@Component
public class UserAITools {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 获取当前日期
     *
     * @return 格式：yyyy-MM-dd
     */
    public String getCurrentDate() {
        String today = LocalDateTime.now().format(DATE_FORMATTER);
        log.info("AI调用工具 → 获取当前日期: {}", today);
        return today;
    }

    /**
     * 获取当前日期和时间
     *
     * @return 格式：yyyy-MM-dd HH:mm:ss
     */
    public String getCurrentDateTime() {
        String now = LocalDateTime.now().format(DATETIME_FORMATTER);
        log.info("AI调用工具 → 获取当前时间: {}", now);
        return now;
    }

    /**
     * 获取时间上下文（星期几、时段、是否是用餐时间）
     *这个方法会返回更丰富的时间信息，帮助AI理解当前场景。
     * @return 时间上下文描述
     */
    public String getTimeContext() {
        LocalDateTime now = LocalDateTime.now();

        // 星期几（中文）
        String dayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA);

        // 当前时间
        String currentTime = now.format(TIME_FORMATTER);

        // 时段判断
        int hour = now.getHour();
        String timeOfDay = getTimeOfDay(hour);

        // 是否是用餐时间
        boolean isMealTime = isMealTime(hour);

        StringBuilder context = new StringBuilder();
        context.append("今天是").append(dayOfWeek).append("，");
        context.append("现在是").append(currentTime).append("，");
        context.append(timeOfDay);

        if (isMealTime) {
            context.append("，是用餐时间");
        }

        String result = context.toString();
        log.info("AI调用工具 → 获取时间上下文: {}", result);
        return result;
    }

    /**
     * 判断当前时段
     */
    private String getTimeOfDay(int hour) {
        if (hour >= 6 && hour < 9) {
            return "早餐时间";
        } else if (hour >= 9 && hour < 11) {
            return "上午";
        } else if (hour >= 11 && hour < 14) {
            return "午餐时间";
        } else if (hour >= 14 && hour < 17) {
            return "下午";
        } else if (hour >= 17 && hour < 20) {
            return "晚餐时间";
        } else if (hour >= 20 && hour < 24) {
            return "夜宵时间";
        } else {
            return "深夜";
        }
    }

    /**
     * 判断是否是用餐时间
     */
    private boolean isMealTime(int hour) {
        return (hour >= 6 && hour < 9) ||    // 早餐
                (hour >= 11 && hour < 14) ||   // 午餐
                (hour >= 17 && hour < 20) ||   // 晚餐
                (hour >= 20 && hour < 24);     // 夜宵
    }
}