package com.suda.AI.tools;

import com.suda.entity.UserSession.RecommendedDish;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 推荐上下文纯解析工具 — 仅保留 {@code parseFromToolResult}，无任何共享状态。
 *
 * <p>v3.0 简化：移除 ThreadLocal 和静态缓冲区（会跨用户污染），
 * 推荐菜品追踪改为通过 {@code UserSession.lastRecommendedDishes} 实现用户级隔离。</p>
 */
public class RecommendationContextHolder {

    private static final Pattern DISH_ID_PATTERN = Pattern.compile("\\[ID:(\\d+)\\]");

    private RecommendationContextHolder() {}

    /** 匹配所有 ID 标记变体 */
    private static final Pattern ALL_ID_MARKERS = Pattern.compile(
            "\\s*\\[ID:\\d+\\]"        // [ID:32]
            + "|\\s*\\[D:\\d+\\]"       // [D:32]
            + "|\\s*[（(]ID[：:]\\d+[）)]" // （ID：32）或 (ID:32)
            + "|\\s*【ID[：:]\\d+】"     // 【ID：32】
            + "|\\s*（ID：\\d+）"        // 全角括号冒号
            + "|\\s*\\(ID:\\d+\\)"        // 半角括号冒号
    );

    /** 从文本中移除所有 ID 标记变体，使其适合展示给用户。 */
    public static String stripMarkers(String text) {
        if (text == null) return null;
        return ALL_ID_MARKERS.matcher(text).replaceAll("");
    }

    /**
     * 从文本中解析 [ID:xxx] 格式的菜品信息（仅用于兜底场景）。
     */
    public static List<RecommendedDish> parseFromToolResult(String text) {
        List<RecommendedDish> dishes = new ArrayList<>();
        if (text == null || text.isBlank()) return dishes;

        for (String line : text.split("\n")) {
            Matcher matcher = DISH_ID_PATTERN.matcher(line);
            if (matcher.find()) {
                Long dishId = Long.parseLong(matcher.group(1));
                String name = extractDishName(line);
                Double price = extractPrice(line);
                dishes.add(new RecommendedDish(dishId, null, name, price));
            }
        }
        return dishes;
    }

    private static String extractDishName(String line) {
        String cleaned = line.replaceFirst("^[\\d]+\\.[\\s]*", "")
                            .replaceFirst("^[-•][\\s]*", "");
        int priceIdx = cleaned.indexOf('¥');
        int dashIdx = cleaned.indexOf('—');
        int bracketIdx = cleaned.indexOf('[');
        int endIdx = cleaned.length();
        if (priceIdx > 0) endIdx = Math.min(endIdx, priceIdx);
        if (dashIdx > 0) endIdx = Math.min(endIdx, dashIdx);
        if (bracketIdx > 0) endIdx = Math.min(endIdx, bracketIdx);
        return cleaned.substring(0, endIdx).trim();
    }

    private static Double extractPrice(String line) {
        Matcher m = Pattern.compile("¥(\\d+\\.?\\d*)").matcher(line);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); }
            catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
