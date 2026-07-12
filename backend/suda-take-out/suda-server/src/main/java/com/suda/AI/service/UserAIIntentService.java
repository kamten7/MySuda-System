package com.suda.AI.service;

import com.suda.AI.tools.CartTools;
import com.suda.AI.tools.DishTools;
import com.suda.AI.tools.RecommendTools;
import com.suda.entity.UserSession.RecommendedDish;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 用户 AI 意图检测与工具预执行服务。
 *
 * <h3>核心设计</h3>
 * <p>不再依赖 AI 模型调用 function calling——所有工具在 Java 层确定性执行：</p>
 * <ol>
 *   <li>用正则 + 关键词检测用户意图</li>
 *   <li>根据意图预执行对应的 Java 工具</li>
 *   <li>返回结构化的工具结果供 Prompt 构建使用</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAIIntentService {

    private final CartTools cartTools;
    private final DishTools dishTools;
    private final RecommendTools recommendTools;

    // ==================== 意图模式 ====================

    /** 加购意图 — 用户明确要求操作购物车 */
    private static final Pattern ADD_TO_CART = Pattern.compile(
            "加入购物车|加到购物车|加进购物车|帮我加|帮我点|帮我下单|来一份|就这个|就它了|"
                    + "加购|添加到购物车|放进购物车|下单|帮我选|我要这个|给我来一份|帮我把.*加入|"
                    + "帮我把.*加到|帮我把.*加进|把.*加入购物车|把.*添加到购物车",
            Pattern.CASE_INSENSITIVE);

    /** 需要确认 — 简短确认词（在已有推荐上下文的情况下） */
    private static final Pattern CONFIRM_SHORT = Pattern.compile(
            "^(需要|要|好的|可以|行|嗯|对|是的|加吧|确认|ok|yes|好呀|好滴|没问题|可以呀|行吧|加一下|加一个)[\\s!！。.~～]*$",
            Pattern.CASE_INSENSITIVE);

    /** 查看购物车 */
    private static final Pattern VIEW_CART = Pattern.compile(
            "查看购物车|购物车有什么|我的购物车|购物车里有什么|看看购物车|打开购物车",
            Pattern.CASE_INSENSITIVE);

    /** 清空购物车 */
    private static final Pattern CLEAR_CART = Pattern.compile(
            "清空购物车|删除购物车|清掉购物车|清理购物车|把购物车清",
            Pattern.CASE_INSENSITIVE);

    /** 推荐意图 */
    private static final Pattern RECOMMEND = Pattern.compile(
            "推荐|今天吃什么|有什么好吃的|吃什么好|现在有什么|帮我推荐|给我推荐|"
                    + "建议|介绍.*菜|推荐.*菜|有没有.*推荐|来点.*推荐|不知道吃|"
                    + "有什么.*推荐|吃什么|选什么",
            Pattern.CASE_INSENSITIVE);

    /** 搜索意图 — 包含菜名、食材、口味、分类等 */
    private static final Pattern SEARCH = Pattern.compile(
            "有没有|想吃|找.*菜|搜|有什么.*菜|有没有.*菜|看看.*菜|看看.*吃|"
                    + "辣的|甜的|酸的|清淡|麻辣|香辣|咸的|不辣|"
                    + "便宜|实惠|贵的|高档|性价比|划算|"
                    + "套餐|单人|双人|家庭|"
                    + "早餐|午餐|晚餐|夜宵|主食|小吃|饮品|甜点|凉菜|热菜|汤|面|饭|粉|"
                    + "川菜|粤菜|湘菜|鲁菜|苏菜|快餐|火锅|烧烤|西餐|日料|"
                    + "爱吃|好吃|喜欢|味道|特色|招牌|人气|热门|"
                    + "东北|西北|南方|北方|广东|四川|湖南|湖北|"
                    + "来个|来一份|来点|找找|找一找",
            Pattern.CASE_INSENSITIVE);

    /**
     * 场景兜底：消息无明显搜索/推荐关键词，但提到了食物相关概念时降级为推荐。
     */
    private static final Pattern FOOD_FALLBACK = Pattern.compile(
            "菜|饭|面|汤|锅|肉|鱼|虾|鸡|鸭|牛|羊|猪|蛋|豆腐|蔬菜|水果|"
                    + "羹|煲|炒|蒸|煮|炸|烤|烧|卤|炖|拌|"
                    + "口味|味道|口感|食材|美食|好吃|吃点|吃点啥|吃啥",
            Pattern.CASE_INSENSITIVE);

    // ==================== 意图枚举 ====================

    public enum Intent {
        ADD_TO_CART,
        VIEW_CART,
        CLEAR_CART,
        RECOMMEND,
        SEARCH,
        CHAT
    }

    /**
     * 意图检测结果，包含意图类型和预执行的工具结果。
     */
    public record IntentResult(
            Intent intent,
            String toolOutput,                 // 工具执行结果文本（可直接注入 Prompt）
            List<RecommendedDish> dishes,       // 本次操作涉及的菜品（用于后续加购）
            boolean cartModified               // 购物车是否被修改
    ) {
        public static IntentResult chat(String output) {
            return new IntentResult(Intent.CHAT, output, Collections.emptyList(), false);
        }
        public static IntentResult withDishes(Intent intent, String output, List<RecommendedDish> dishes) {
            return new IntentResult(intent, output, dishes, false);
        }
        public static IntentResult cartAction(Intent intent, String output, boolean modified) {
            return new IntentResult(intent, output, Collections.emptyList(), modified);
        }
    }

    // ==================== 主入口 ====================

    /**
     * 检测用户意图并预执行对应的工具。
     *
     * @param userMessage       用户消息
     * @param recommendedDishes 上一轮推荐的菜品（可能为空）
     * @param userId            当前用户 ID
     * @return 意图检测结果
     */
    public IntentResult detectAndExecute(String userMessage,
                                         List<RecommendedDish> recommendedDishes,
                                         Long userId) {
        String msg = userMessage != null ? userMessage.trim() : "";

        // 1. 检查是否为确认意图（短确认词 + 有推荐上下文）
        if (CONFIRM_SHORT.matcher(msg).matches()
                && recommendedDishes != null && !recommendedDishes.isEmpty()) {
            return executeAddToCart(msg, recommendedDishes, userId);
        }

        // 2. 加购意图（显式包含"加入购物车"等关键词）
        if (ADD_TO_CART.matcher(msg).find()) {
            // 尝试从消息中提取菜名
            List<RecommendedDish> dishes = extractDishNamesAndSearch(msg, recommendedDishes);
            if (!dishes.isEmpty()) {
                return executeAddToCart(msg, dishes, userId);
            }
            // 有推荐上下文但没提取到菜名，用推荐上下文
            if (recommendedDishes != null && !recommendedDishes.isEmpty()) {
                return executeAddToCart(msg, recommendedDishes, userId);
            }
            // 没有可用的菜品
            return new IntentResult(Intent.ADD_TO_CART,
                    "用户想加购但没有找到可用的菜品ID，需要提示用户具体说明菜名",
                    Collections.emptyList(), false);
        }

        // 3. 查看购物车
        if (VIEW_CART.matcher(msg).find()) {
            String result = cartTools.getCart();
            return IntentResult.cartAction(Intent.VIEW_CART, result, false);
        }

        // 4. 清空购物车
        if (CLEAR_CART.matcher(msg).find()) {
            String result = cartTools.clearCart();
            return IntentResult.cartAction(Intent.CLEAR_CART, result, true);
        }

        // 5. 推荐意图
        if (RECOMMEND.matcher(msg).find()) {
            return executeRecommend(msg);
        }

        // 6. 搜索意图
        if (SEARCH.matcher(msg).find()) {
            return executeSearch(msg);
        }

        // 7. 兜底：消息包含食物相关词汇但没匹配到具体意图 → 降级为推荐
        if (FOOD_FALLBACK.matcher(msg).find()) {
            return executeRecommend(msg);
        }

        // 8. 默认：聊天
        return IntentResult.chat(null);
    }

    // ==================== 工具执行 ====================

    private IntentResult executeAddToCart(String msg, List<RecommendedDish> dishes, Long userId) {
        // 判断用户指定了哪个（"要第一个"、"都要"等）
        int targetIndex = extractDishIndex(msg, dishes.size());

        StringBuilder result = new StringBuilder();
        if (targetIndex >= 0) {
            // 用户指定了第N个 → 只加那一个
            RecommendedDish d = dishes.get(targetIndex);
            String r = cartTools.addToCart(d.getDishId(), d.getSetmealId(), null, 1);
            result.append(r);
            log.info("加购(指定序号{}): dishId={}, name='{}'", targetIndex + 1, d.getDishId(), d.getName());
        } else {
            // 未指定序号 → 全部加入购物车（用户确认推荐后说"帮我加"就该全加）
            result.append("✅ 已添加以下菜品到购物车：\n");
            for (RecommendedDish d : dishes) {
                cartTools.addToCart(d.getDishId(), d.getSetmealId(), null, 1);
                result.append(String.format("- %s  ¥%.2f\n",
                        d.getName(), d.getPrice() != null ? d.getPrice() : 0));
                log.info("加购(批量): dishId={}, name='{}'", d.getDishId(), d.getName());
            }
            log.info("加购完成：共 {} 道菜品", dishes.size());
        }

        return IntentResult.cartAction(Intent.ADD_TO_CART, result.toString(), true);
    }

    private IntentResult executeRecommend(String msg) {
        // 提取可能的偏好参数
        String flavor = extractFlavor(msg);
        String scenario = extractScenario(msg);

        String result = recommendTools.getRecommendations(flavor, scenario, null, null, 5);
        // 从工具结果中解析菜品ID（用于后续加购追踪）
        List<RecommendedDish> dishes = com.suda.AI.tools.RecommendationContextHolder.parseFromToolResult(result);

        // 如果推荐结果为空，降级为热门菜品
        if (dishes.isEmpty()) {
            result = dishTools.getHotDishes(5);
            dishes = com.suda.AI.tools.RecommendationContextHolder.parseFromToolResult(result);
            log.info("推荐降级为热门菜品: {} 道", dishes.size());
        }

        // 去除 [ID:xxx] 内部标记，不暴露给 AI 和用户
        String cleanResult = com.suda.AI.tools.RecommendationContextHolder.stripMarkers(result);

        log.info("推荐执行完成: {} 道菜品", dishes.size());
        return IntentResult.withDishes(Intent.RECOMMEND, cleanResult, dishes);
    }

    private IntentResult executeSearch(String msg) {
        // 提取关键词
        String keyword = extractSearchKeyword(msg);
        String flavor = extractFlavor(msg);

        String result = dishTools.searchDishes(keyword, null, flavor, 5);
        List<RecommendedDish> dishes = com.suda.AI.tools.RecommendationContextHolder.parseFromToolResult(result);

        if (dishes.isEmpty() && keyword != null) {
            // 关键词搜索无结果，尝试名称匹配
            List<String> names = Collections.singletonList(keyword);
            dishes = dishTools.findDishesByNames(names);
            if (!dishes.isEmpty()) {
                log.info("关键词搜索无结果，名称匹配找到 {} 道菜品", dishes.size());
                // 构建结果文本（无 [ID:xxx] 标记）
                StringBuilder sb = new StringBuilder("找到以下菜品：\n");
                for (int i = 0; i < dishes.size(); i++) {
                    RecommendedDish d = dishes.get(i);
                    sb.append(String.format("%d. %s  ¥%.2f\n",
                            i + 1, d.getName(), d.getPrice() != null ? d.getPrice() : 0));
                }
                result = sb.toString();
            }
        }

        // 去除 [ID:xxx] 内部标记
        String cleanResult = com.suda.AI.tools.RecommendationContextHolder.stripMarkers(result);

        log.info("搜索执行完成: keyword='{}', flavor='{}', {} 道菜品", keyword, flavor, dishes.size());
        return IntentResult.withDishes(Intent.SEARCH, cleanResult, dishes);
    }

    // ==================== 辅助方法 ====================

    /**
     * 从用户消息中提取菜名，并通过数据库匹配找到真实菜品ID。
     * 优先使用推荐上下文，其次从文本中提取菜名后查数据库。
     */
    private List<RecommendedDish> extractDishNamesAndSearch(String msg,
                                                            List<RecommendedDish> recommendedDishes) {
        // 如果有推荐上下文，检查用户是否指定了具体的菜
        if (recommendedDishes != null && !recommendedDishes.isEmpty()) {
            // 用户说"全部"、"都"意味着所有推荐菜品
            if (Pattern.compile("全部|都|所有|每个|这些|它们|他们").matcher(msg).find()) {
                return new ArrayList<>(recommendedDishes);
            }
            // 用户说"第一个"、"第二个"等
            int idx = extractDishIndex(msg, recommendedDishes.size());
            if (idx >= 0) {
                return Collections.singletonList(recommendedDishes.get(idx));
            }
        }

        // 提取菜名：匹配 **菜名**、「菜名」、"菜名" 以及直接菜名
        List<String> names = new ArrayList<>();
        java.util.regex.Matcher m = Pattern.compile(
                "\\*\\*(.+?)\\*\\*|「(.+?)」|\"(.+?)\"|把(.+?)加入|把(.+?)加到|把(.+?)加进")
                .matcher(msg);
        while (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                String name = m.group(i);
                if (name != null && name.length() >= 2 && name.length() <= 20) {
                    names.add(name.trim());
                }
            }
        }

        // 如果没匹配到任何格式，尝试从句子中提取可能的菜名
        // "帮我把麻辣香锅加入购物车" → 直接取"把...加入"中间的词
        if (names.isEmpty()) {
            m = Pattern.compile("把([^加入加到加进购物车]{2,10})[加入加到加进购物车]").matcher(msg);
            if (m.find() && m.group(1) != null) {
                String name = m.group(1).trim().replaceAll("[的啦哦呢]", "");
                if (name.length() >= 2) names.add(name);
            }
        }

        if (names.isEmpty()) return Collections.emptyList();

        // 查数据库
        List<RecommendedDish> found = dishTools.findDishesByNames(names);
        return found != null ? found : Collections.emptyList();
    }

    /** 从搜索类消息中提取核心关键词（地域/食材/口味等有意义的词）。 */
    private String extractSearchKeyword(String msg) {
        // 先提取有意义的实体词，按优先级
        // 地域：东北/四川/广东/西北/... 匹配
        java.util.regex.Matcher region = Pattern.compile(
                "东北|西北|西南|华东|华南|华北|华中|"
                        + "四川|广东|湖南|湖北|云南|贵州|陕西|山西|山东|河南|河北|"
                        + "江苏|浙江|福建|海南|新疆|西藏|内蒙|广西|宁夏|"
                        + "川|粤|湘|鄂|鲁|苏|闽|沪|京|港|台|日|韩|泰|东南亚"
        ).matcher(msg);
        if (region.find()) return region.group();

        // 菜品类：面/饭/汤/锅/...
        java.util.regex.Matcher foodType = Pattern.compile(
                "盖饭|炒饭|拌饭|拉面|汤面|拌面|米粉|米线|饺子|包子|馒头|饼|粥|"
                        + "火锅|干锅|砂锅|麻辣烫|冒菜|烤鱼|小龙虾|"
                        + "汉堡|披萨|寿司|刺身|拉面|烤肉|炸鸡|"
                        + "凉菜|热菜|小炒|蒸菜|炖菜|烧菜|卤味|烧烤|"
                        + "饮料|奶茶|咖啡|果汁|甜品|蛋糕|冰淇淋"
        ).matcher(msg);
        if (foodType.find()) return foodType.group();

        // 食材类
        java.util.regex.Matcher ingredient = Pattern.compile(
                "牛肉|猪肉|羊肉|鸡肉|鸭肉|鱼肉|虾|蟹|贝|鱿鱼|牛蛙|"
                        + "豆腐|鸡蛋|土豆|番茄|黄瓜|白菜|青菜|西兰花|"
                        + "茄子|豆角|青椒|辣椒|花生|木耳|香菇"
        ).matcher(msg);
        if (ingredient.find()) return ingredient.group();

        // 口感/做法
        java.util.regex.Matcher style = Pattern.compile(
                "麻辣|香辣|酸辣|糖醋|鱼香|宫保|回锅|水煮|干煸|红烧|清蒸|"
                        + "爆炒|酱香|蒜蓉|葱油|红烧|白切|盐焗|"
                        + "清淡|爽口|酥脆|软糯|鲜嫩|浓郁"
        ).matcher(msg);
        if (style.find()) return style.group();

        // 都没有 -> 移除口语前缀后返回
        String cleaned = msg
                .replaceAll("再看看|再看看有|有什么|有没有|帮我|给我|推荐|找找|找一找|"
                        + "想吃|想点|来点|来个|看看|看一下|瞧瞧", "")
                .replaceAll("[的啦哦呢吧吗啊呀是]", "")
                .trim();
        if (cleaned.isEmpty() || cleaned.length() > 10) {
            // 还是太长或空 → 用原始消息的核心词
            return msg.replaceAll("[的啦哦呢吧吗啊呀是再看看有没有有什么帮我给我]", "").trim();
        }
        return cleaned;
    }

    /** 提取口味偏好 */
    private String extractFlavor(String msg) {
        if (Pattern.compile("辣|麻辣|香辣|酸辣|微辣|中辣|特辣").matcher(msg).find()) return "辣";
        if (Pattern.compile("甜|甜品|甜点|糖").matcher(msg).find()) return "甜";
        if (Pattern.compile("清淡|不辣|素食|健康|减肥|低卡|减脂").matcher(msg).find()) return "清淡";
        if (Pattern.compile("酸|酸的").matcher(msg).find()) return "酸";
        return null;
    }

    /** 提取场景 */
    private String extractScenario(String msg) {
        if (Pattern.compile("热|夏天|暑|凉快|凉爽").matcher(msg).find()) return "夏天";
        if (Pattern.compile("冷|冬天|寒|暖|暖和").matcher(msg).find()) return "冬天";
        if (Pattern.compile("减肥|瘦|轻食|低卡|减脂|健康|素食").matcher(msg).find()) return "减肥";
        if (Pattern.compile("聚会|请客|朋友|聚餐").matcher(msg).find()) return "聚会";
        if (Pattern.compile("实惠|便宜|省钱|性价比|便宜点").matcher(msg).find()) return "实惠";
        return null;
    }

    /** 提取菜品序号 */
    private int extractDishIndex(String msg, int maxCount) {
        if (msg == null || maxCount <= 0) return -1;
        Map<String, Integer> cnNums = Map.of(
                "一", 1, "二", 2, "三", 3, "四", 4, "五", 5,
                "六", 6, "七", 7, "八", 8, "九", 9, "十", 10);
        java.util.regex.Matcher m = Pattern.compile("第\\s*([0-9一二三四五六七八九十]+)\\s*个").matcher(msg);
        if (m.find()) {
            String numStr = m.group(1);
            try { int idx = Integer.parseInt(numStr); if (idx >= 1 && idx <= maxCount) return idx - 1; }
            catch (NumberFormatException e) {
                int idx = cnNums.getOrDefault(numStr, -1);
                if (idx >= 1 && idx <= maxCount) return idx - 1;
            }
        }
        return -1;
    }
}
