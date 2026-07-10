package com.suda.AI.config;

import com.suda.AI.tools.CartTools;
import com.suda.AI.tools.DishTools;
import com.suda.AI.tools.RecommendTools;
import com.suda.AI.tools.UserAITools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring AI 1.0.0-M5 配置
 *
 * <p>使用 {@link FunctionCallback} 包装工具类方法。通过 Spring 的
 * {@code List<FunctionCallback>} 自动收集所有 FunctionCallback Bean，
 * 统一注册到 {@link ChatClient} 中。</p>
 *
 * <p>这样避免了逐个注入同类型 Bean 时依赖 -parameters 编译参数的隐患。
 * 新增工具只需添加对应的 {@link FunctionCallback} {@code @Bean} 即可自动生效。</p>
 */
@Configuration
public class SpringAIConfig {

    /**
     * 构建 ChatClient，自动收集上下文中所有 {@link FunctionCallback} Bean。
     *
     * @param chatModel        Spring AI OpenAI 聊天模型
     * @param functionCallbacks 所有注册的 FunctionCallback Bean（自动按类型收集）
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel,
                                 List<FunctionCallback> functionCallbacks) {

        return ChatClient.builder(chatModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("deepseek-chat")
                        .temperature(0.7)
                        .maxTokens(3000)
                        .build())
                .defaultFunctions(functionCallbacks.toArray(new FunctionCallback[0]))
                .build();
    }

    // ==================== 时间工具 FunctionCallbacks ====================

    @Bean
    public FunctionCallback getCurrentDateCallback(UserAITools userAITools) {
        return FunctionCallback.builder()
                .method("getCurrentDate")
                .targetObject(userAITools)
                .name("getCurrentDate")
                .description("获取当前真实日期（格式：yyyy-MM-dd）。AI训练数据有截止日期，不知道'今天'是哪一天，处理任何时间相关的查询（今天/本周/最近等）时必须先调用此函数。")
                .build();
    }

    @Bean
    public FunctionCallback getCurrentDateTimeCallback(UserAITools userAITools) {
        return FunctionCallback.builder()
                .method("getCurrentDateTime")
                .targetObject(userAITools)
                .name("getCurrentDateTime")
                .description("获取当前日期和时间（格式：yyyy-MM-dd HH:mm:ss）")
                .build();
    }

    @Bean
    public FunctionCallback getTimeContextCallback(UserAITools userAITools) {
        return FunctionCallback.builder()
                .method("getTimeContext")
                .targetObject(userAITools)
                .name("getTimeContext")
                .description("获取当前时间上下文，包括星期几、当前时段（早餐/午餐/晚餐/夜宵等）、是否是用餐时间。适合用户问'今天吃什么'、'现在有什么'等时间相关推荐时调用")
                .build();
    }

    // ==================== 菜品查询 FunctionCallbacks ====================

    @Bean
    public FunctionCallback searchDishesCallback(DishTools dishTools) {
        return FunctionCallback.builder()
                .method("searchDishes", String.class, Long.class, String.class, Integer.class)
                .targetObject(dishTools)
                .name("searchDishes")
                .description("搜索菜品，根据关键词（菜名、食材、口味）、分类ID或口味筛选。支持空格分隔多关键词。返回菜品列表（名称、价格、描述）")
                .build();
    }

    @Bean
    public FunctionCallback getDishDetailCallback(DishTools dishTools) {
        return FunctionCallback.builder()
                .method("getDishDetail", Long.class)
                .targetObject(dishTools)
                .name("getDishDetail")
                .description("获取菜品详细信息，包括名称、价格、口味、描述、分类")
                .build();
    }

    @Bean
    public FunctionCallback getCategoriesCallback(DishTools dishTools) {
        return FunctionCallback.builder()
                .method("getCategories")
                .targetObject(dishTools)
                .name("getCategories")
                .description("获取所有在售菜品分类列表（如川菜、粤菜、快餐等）")
                .build();
    }

    @Bean
    public FunctionCallback getHotDishesCallback(DishTools dishTools) {
        return FunctionCallback.builder()
                .method("getHotDishes", Integer.class)
                .targetObject(dishTools)
                .name("getHotDishes")
                .description("获取热门菜品推荐（在售菜品），适合用户想要'最受欢迎'的菜品时调用")
                .build();
    }

    @Bean
    public FunctionCallback searchByPriceRangeCallback(DishTools dishTools) {
        return FunctionCallback.builder()
                .method("searchByPriceRange", Double.class, Double.class, Integer.class)
                .targetObject(dishTools)
                .name("searchByPriceRange")
                .description("按价格范围搜索菜品，适合预算敏感的用户")
                .build();
    }

    // ==================== 智能推荐 FunctionCallbacks ====================

    @Bean
    public FunctionCallback getRecommendationsCallback(RecommendTools recommendTools) {
        return FunctionCallback.builder()
                .method("getRecommendations", String.class, String.class, Double.class, Double.class, Integer.class)
                .targetObject(recommendTools)
                .name("getRecommendations")
                .description("智能推荐菜品，根据用户的口味偏好、场景（如天热/天冷/聚会/减肥）、价格范围等条件推荐。推荐结果会包含'是否需要加入购物车'的询问")
                .build();
    }

    @Bean
    public FunctionCallback getBudgetFriendlyCallback(RecommendTools recommendTools) {
        return FunctionCallback.builder()
                .method("getBudgetFriendlyDishes", Double.class, Integer.class)
                .targetObject(recommendTools)
                .name("getBudgetFriendlyDishes")
                .description("获取实惠菜品推荐，适合追求性价比的用户。推荐结果会包含'是否需要加入购物车'的询问")
                .build();
    }

    // ==================== 购物车 FunctionCallbacks ====================

    @Bean
    public FunctionCallback addToCartCallback(CartTools cartTools) {
        return FunctionCallback.builder()
                .method("addToCart", Long.class, Long.class, String.class, Integer.class)
                .targetObject(cartTools)
                .name("addToCart")
                .description("将菜品添加到购物车。⚠️ 仅在用户明确要求'加入购物车'/'帮我加'/'下单'时调用。需要提供菜品ID或套餐ID、可选口味和数量")
                .build();
    }

    @Bean
    public FunctionCallback removeFromCartCallback(CartTools cartTools) {
        return FunctionCallback.builder()
                .method("removeFromCart", Long.class, Long.class)
                .targetObject(cartTools)
                .name("removeFromCart")
                .description("从购物车减少或移除指定菜品/套餐。⚠️ 仅在用户明确要求时调用")
                .build();
    }

    @Bean
    public FunctionCallback getCartCallback(CartTools cartTools) {
        return FunctionCallback.builder()
                .method("getCart")
                .targetObject(cartTools)
                .name("getCart")
                .description("查看当前购物车内容，返回菜品列表和总价")
                .build();
    }

    @Bean
    public FunctionCallback clearCartCallback(CartTools cartTools) {
        return FunctionCallback.builder()
                .method("clearCart")
                .targetObject(cartTools)
                .name("clearCart")
                .description("清空购物车中所有商品。⚠️ 仅在用户明确要求时调用")
                .build();
    }

    @Bean
    public FunctionCallback getCartCountCallback(CartTools cartTools) {
        return FunctionCallback.builder()
                .method("getCartCount")
                .targetObject(cartTools)
                .name("getCartCount")
                .description("获取购物车中商品总数量")
                .build();
    }
}
