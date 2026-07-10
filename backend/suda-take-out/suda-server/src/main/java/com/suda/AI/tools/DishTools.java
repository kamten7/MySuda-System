package com.suda.AI.tools;

import com.suda.entity.Category;
import com.suda.entity.Dish;
import com.suda.entity.DishFlavor;
import com.suda.service.CategoryService;
import com.suda.service.DishService;
import com.suda.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜品查询工具 —— 供 AI 助手调用，提供菜品搜索、详情、分类、热门排行等能力。
 *
 * <p>方法通过 {@link org.springframework.ai.model.function.FunctionCallback} 注册到 ChatClient。
 * 每个 public 方法对应一个 AI 可调用的工具函数。</p>
 */
@Component
@RequiredArgsConstructor
public class DishTools {

    private final DishService dishService;
    private final CategoryService categoryService;

    /**
     * 多关键词模糊搜索菜品。
     * 支持按名称、口味、分类筛选，关键词用空格分隔以支持多词匹配。
     */
    public String searchDishes(String keyword, Long categoryId, String flavor, Integer limit) {
        Dish dish = new Dish();
        dish.setStatus(1); // 只查在售菜品
        if (categoryId != null) {
            dish.setCategoryId(categoryId);
        }

        List<DishVO> dishes = dishService.listWithFlavor(dish);

        // 关键词匹配（忽略大小写，支持空格分隔多词）
        if (keyword != null && !keyword.isBlank()) {
            String[] words = keyword.toLowerCase().split("\\s+");
            dishes = dishes.stream()
                    .filter(d -> {
                        String name = d.getName() != null ? d.getName().toLowerCase() : "";
                        String desc = d.getDescription() != null ? d.getDescription().toLowerCase() : "";
                        String flavors = flattenFlavors(d.getFlavors()).toLowerCase();

                        for (String w : words) {
                            if (name.contains(w) || desc.contains(w) || flavors.contains(w)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // 口味偏好过滤（在菜品关联的 DishFlavor 列表中查找）
        if (flavor != null && !flavor.isBlank()) {
            String f = flavor.toLowerCase();
            dishes = dishes.stream()
                    .filter(d -> d.getFlavors() != null && d.getFlavors().stream()
                            .anyMatch(df -> df.getName() != null && df.getName().toLowerCase().contains(f)
                                    || df.getValue() != null && df.getValue().toLowerCase().contains(f)))
                    .collect(Collectors.toList());
        }

        int count = (limit != null && limit > 0) ? limit : 5;
        dishes = dishes.stream().limit(count).collect(Collectors.toList());

        if (dishes.isEmpty()) {
            return "未找到符合条件的菜品，可以试试换一个关键词或放宽条件 😊";
        }

        StringBuilder result = new StringBuilder("找到以下菜品：\n");
        for (int i = 0; i < dishes.size(); i++) {
            DishVO d = dishes.get(i);
            String desc = d.getDescription() != null && !d.getDescription().isEmpty()
                    ? " — " + d.getDescription() : "";
            result.append(String.format("%d. %s  ¥%.2f%s\n", i + 1, d.getName(), d.getPrice(), desc));
        }
        return result.toString();
    }

    /**
     * 获取菜品详细信息。
     */
    public String getDishDetail(Long dishId) {
        DishVO dish = dishService.getByIdWithFlavor(dishId);
        if (dish == null) {
            return "菜品不存在或已下架";
        }

        String categoryName = dish.getCategoryName();
        if (categoryName == null && dish.getCategoryId() != null) {
            categoryName = lookupCategoryName(dish.getCategoryId());
        }

        return String.format("""
                        📋 菜品详情
                        名称：%s
                        价格：¥%.2f
                        分类：%s
                        口味：%s
                        描述：%s""",
                dish.getName(),
                dish.getPrice(),
                categoryName != null ? categoryName : "未分类",
                dish.getFlavors() != null && !dish.getFlavors().isEmpty()
                        ? flattenFlavors(dish.getFlavors()) : "无特殊口味",
                dish.getDescription() != null ? dish.getDescription() : "暂无描述");
    }

    /**
     * 获取所有在售菜品分类列表。
     */
    public String getCategories() {
        List<Category> categories = categoryService.list(1);
        if (categories == null || categories.isEmpty()) {
            return "暂无可用分类";
        }

        List<Category> activeCategories = categories.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .collect(Collectors.toList());

        if (activeCategories.isEmpty()) {
            return "暂无可用分类";
        }

        StringBuilder result = new StringBuilder("当前可选的菜品分类：\n");
        for (int i = 0; i < activeCategories.size(); i++) {
            result.append(String.format("%d. %s\n", i + 1, activeCategories.get(i).getName()));
        }
        return result.toString();
    }

    /**
     * 获取热门菜品推荐。
     */
    public String getHotDishes(Integer limit) {
        Dish dish = new Dish();
        dish.setStatus(1);

        List<DishVO> dishes = dishService.listWithFlavor(dish);
        int count = (limit != null && limit > 0) ? limit : 5;
        dishes = dishes.stream().limit(count).collect(Collectors.toList());

        if (dishes.isEmpty()) {
            return "暂无在售菜品";
        }

        StringBuilder result = new StringBuilder("🔥 热门菜品推荐：\n");
        for (int i = 0; i < dishes.size(); i++) {
            DishVO d = dishes.get(i);
            result.append(String.format("%d. %s — ¥%.2f\n", i + 1, d.getName(), d.getPrice()));
        }
        return result.toString();
    }

    /**
     * 按价格范围搜索菜品。
     */
    public String searchByPriceRange(Double minPrice, Double maxPrice, Integer limit) {
        Dish dish = new Dish();
        dish.setStatus(1);

        List<DishVO> dishes = dishService.listWithFlavor(dish);

        double min = minPrice != null ? minPrice : 0;
        double max = maxPrice != null ? maxPrice : Double.MAX_VALUE;

        dishes = dishes.stream()
                .filter(d -> d.getPrice() != null
                        && d.getPrice().doubleValue() >= min
                        && d.getPrice().doubleValue() <= max)
                .collect(Collectors.toList());

        int count = (limit != null && limit > 0) ? limit : 5;
        dishes = dishes.stream().limit(count).collect(Collectors.toList());

        if (dishes.isEmpty()) {
            return String.format("¥%.0f ~ ¥%.0f 价格范围内暂无菜品", min, max);
        }

        StringBuilder result = new StringBuilder(String.format("¥%.0f ~ ¥%.0f 的菜品：\n", min, max));
        for (DishVO d : dishes) {
            result.append(String.format("- %s  ¥%.2f\n", d.getName(), d.getPrice()));
        }
        return result.toString();
    }

    // ==================== 内部辅助方法 ====================

    private String flattenFlavors(List<DishFlavor> flavors) {
        if (flavors == null || flavors.isEmpty()) return "";
        return flavors.stream()
                .map(f -> (f.getName() != null ? f.getName() : "")
                        + (f.getValue() != null ? ":" + f.getValue() : ""))
                .collect(Collectors.joining("，"));
    }

    private String lookupCategoryName(Long categoryId) {
        try {
            List<Category> categories = categoryService.list(1);
            if (categories != null) {
                return categories.stream()
                        .filter(c -> categoryId.equals(c.getId()))
                        .map(Category::getName)
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
