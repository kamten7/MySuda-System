package com.suda.AI.tools;

import com.suda.entity.Category;
import com.suda.entity.Dish;
import com.suda.service.CategoryService;
import com.suda.service.DishService;
import com.suda.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推荐工具 —— 根据用户偏好、场景（天气/季节/心情）推荐菜品。
 *
 * <p>方法通过 {@link org.springframework.ai.model.function.FunctionCallback} 注册到 ChatClient。</p>
 */
@Component
@RequiredArgsConstructor
public class RecommendTools {

    private final DishService dishService;
    private final CategoryService categoryService;

    /**
     * 综合智能推荐，支持口味、价格、场景等多维度条件。
     * 推荐后会提示是否需要加入购物车。
     */
    public String getRecommendations(String flavorPreference, String scenario,
                                     Double minPrice, Double maxPrice, Integer limit) {
        Dish dish = new Dish();
        dish.setStatus(1);

        List<DishVO> allDishes = dishService.listWithFlavor(dish);

        // 按价格筛选
        double min = minPrice != null ? minPrice : 0;
        double max = maxPrice != null ? maxPrice : Double.MAX_VALUE;
        List<DishVO> candidates = allDishes.stream()
                .filter(d -> d.getPrice() != null
                        && d.getPrice().doubleValue() >= min
                        && d.getPrice().doubleValue() <= max)
                .collect(Collectors.toList());

        // 场景感知推荐
        String scenarioHint = "";
        if (scenario != null && !scenario.isBlank()) {
            String s = scenario.toLowerCase();
            if (s.contains("热") || s.contains("夏天") || s.contains("暑")) {
                candidates = prioritizeCategory(candidates, "饮品", "凉菜", "沙拉");
                scenarioHint = "🧊 天热推荐清爽解暑的菜品：\n";
            } else if (s.contains("冷") || s.contains("冬天") || s.contains("寒")) {
                candidates = prioritizeCategory(candidates, "汤", "火锅", "煲");
                scenarioHint = "🍲 天冷推荐暖身暖心的菜品：\n";
            } else if (s.contains("减肥") || s.contains("瘦") || s.contains("轻食")) {
                candidates = prioritizeCategory(candidates, "沙拉", "蒸菜", "素食");
                scenarioHint = "🥗 为您推荐健康低卡的菜品：\n";
            } else if (s.contains("聚会") || s.contains("请客") || s.contains("朋友")) {
                candidates = prioritizeCategory(candidates, "招牌", "荤菜", "干锅");
                scenarioHint = "🎉 聚会推荐，丰盛又体面：\n";
            }
        }

        // 口味偏好排序
        if (flavorPreference != null && !flavorPreference.isBlank()) {
            String fp = flavorPreference.toLowerCase();
            candidates = candidates.stream()
                    .sorted((a, b) -> {
                        boolean aMatch = dishMatchesFlavor(a, fp);
                        boolean bMatch = dishMatchesFlavor(b, fp);
                        return Boolean.compare(bMatch, aMatch);
                    })
                    .collect(Collectors.toList());
        }

        // 多样性：尽量不同分类各取一些
        int count = (limit != null && limit > 0) ? limit : 3;
        List<DishVO> diverse = selectDiverse(candidates, count);

        if (diverse.isEmpty()) {
            return "暂无符合条件的推荐菜品，可以试试调整偏好或预算哦 😊";
        }

        StringBuilder result = new StringBuilder();
        if (!scenarioHint.isEmpty()) {
            result.append(scenarioHint).append("\n");
        } else {
            result.append("为您推荐以下菜品：\n");
        }

        for (int i = 0; i < diverse.size(); i++) {
            DishVO d = diverse.get(i);
            String reason = buildRecommendReason(d, flavorPreference, scenario);
            result.append(String.format("%d. %s — ¥%.2f（%s）\n",
                    i + 1, d.getName(), d.getPrice(), reason));
        }

        result.append("\n需要我帮您把喜欢的菜品加入购物车吗？");
        return result.toString();
    }

    /**
     * 获取实惠菜品推荐，适合追求性价比的用户。
     */
    public String getBudgetFriendlyDishes(Double maxPrice, Integer limit) {
        double price = maxPrice != null ? maxPrice : 30.0;
        int count = (limit != null && limit > 0) ? limit : 5;

        Dish dish = new Dish();
        dish.setStatus(1);

        List<DishVO> dishes = dishService.listWithFlavor(dish);
        dishes = dishes.stream()
                .filter(d -> d.getPrice() != null && d.getPrice().doubleValue() <= price)
                .sorted(Comparator.comparing(d -> d.getPrice().doubleValue()))
                .limit(count)
                .collect(Collectors.toList());

        if (dishes.isEmpty()) {
            return String.format("暂无 ¥%.0f 以下的菜品", price);
        }

        StringBuilder result = new StringBuilder(String.format("💰 为您推荐 ¥%.0f 以下的实惠菜品：\n", price));
        for (DishVO d : dishes) {
            result.append(String.format("- %s（¥%.2f）\n", d.getName(), d.getPrice()));
        }
        result.append("\n需要帮您加入购物车吗？");
        return result.toString();
    }

    // ==================== 内部辅助方法 ====================

    private boolean dishMatchesFlavor(DishVO dish, String flavorPreference) {
        if (dish.getFlavors() == null) return false;
        return dish.getFlavors().stream()
                .anyMatch(f -> {
                    String name = f.getName() != null ? f.getName().toLowerCase() : "";
                    String value = f.getValue() != null ? f.getValue().toLowerCase() : "";
                    return name.contains(flavorPreference) || value.contains(flavorPreference);
                });
    }

    private List<DishVO> selectDiverse(List<DishVO> candidates, int count) {
        if (candidates.size() <= count) return new ArrayList<>(candidates);

        List<DishVO> result = new ArrayList<>();
        Set<Long> usedCategory = new HashSet<>();
        List<DishVO> remaining = new ArrayList<>(candidates);

        for (DishVO d : candidates) {
            if (result.size() >= count) break;
            if (!usedCategory.contains(d.getCategoryId())) {
                result.add(d);
                usedCategory.add(d.getCategoryId());
                remaining.remove(d);
            }
        }

        for (DishVO d : remaining) {
            if (result.size() >= count) break;
            result.add(d);
        }

        return result;
    }

    private List<DishVO> prioritizeCategory(List<DishVO> candidates, String... targetCategoryNames) {
        if (candidates.isEmpty()) return candidates;

        Set<String> targets = new HashSet<>(Arrays.asList(targetCategoryNames));
        Map<Long, String> categoryNameCache = new HashMap<>();

        try {
            List<Category> allCategories = categoryService.list(1);
            if (allCategories != null) {
                allCategories.forEach(c -> categoryNameCache.put(c.getId(), c.getName()));
            }
        } catch (Exception ignored) {
        }

        return candidates.stream()
                .sorted((a, b) -> {
                    String catA = categoryNameCache.getOrDefault(a.getCategoryId(), "");
                    String catB = categoryNameCache.getOrDefault(b.getCategoryId(), "");
                    boolean aMatch = targets.stream().anyMatch(t -> catA != null && catA.contains(t));
                    boolean bMatch = targets.stream().anyMatch(t -> catB != null && catB.contains(t));
                    return Boolean.compare(bMatch, aMatch);
                })
                .collect(Collectors.toList());
    }

    private String buildRecommendReason(DishVO dish, String flavorPreference, String scenario) {
        if (scenario != null) {
            String s = scenario.toLowerCase();
            if (s.contains("热") || s.contains("夏天")) return "清爽解暑";
            if (s.contains("冷") || s.contains("冬天")) return "暖心暖胃";
            if (s.contains("减肥") || s.contains("瘦")) return "低卡健康";
            if (s.contains("聚会") || s.contains("请客")) return "聚餐首选";
        }
        if (flavorPreference != null) {
            String fp = flavorPreference.toLowerCase();
            if (fp.contains("辣")) return "麻辣鲜香";
            if (fp.contains("清淡")) return "清爽健康";
            if (fp.contains("甜")) return "甜蜜可口";
            if (fp.contains("酸")) return "开胃爽口";
        }
        return "人气推荐";
    }
}
