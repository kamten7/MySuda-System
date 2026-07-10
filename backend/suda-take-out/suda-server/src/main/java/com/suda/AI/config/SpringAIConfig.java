package com.suda.AI.config;

import com.suda.AI.tools.CartTools;
import com.suda.AI.tools.DishTools;
import com.suda.AI.tools.RecommendTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 1.0.0-M5 配置
 *
 * <p>使用 {@link FunctionCallback} 包装工具类方法，通过
 * {@link ChatClient.Builder#defaultFunctions(FunctionCallback...)} 注册到 ChatClient。</p>
 *
 * <p>AI 会根据 FunctionCallback 的 name/description 自动决定何时调用哪个工具。
 * 购物车操作的实际限制由 System Prompt 中的规则控制。</p>
 */
@Configuration
public class SpringAIConfig {

    // ==================== ChatClient ====================

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel,
                                 FunctionCallback searchDishesCallback,
                                 FunctionCallback getDishDetailCallback,
                                 FunctionCallback getCategoriesCallback,
                                 FunctionCallback getHotDishesCallback,
                                 FunctionCallback searchByPriceRangeCallback,
                                 FunctionCallback getRecommendationsCallback,
                                 FunctionCallback getBudgetFriendlyCallback,
                                 FunctionCallback addToCartCallback,
                                 FunctionCallback removeFromCartCallback,
                                 FunctionCallback getCartCallback,
                                 FunctionCallback clearCartCallback,
                                 FunctionCallback getCartCountCallback) {

        return ChatClient.builder(chatModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("deepseek-chat")
                        .temperature(0.7)
                        .maxTokens(3000)
                        .build())
                .defaultFunctions(
                        // 菜品查询工具
                        searchDishesCallback,
                        getDishDetailCallback,
                        getCategoriesCallback,
                        getHotDishesCallback,
                        searchByPriceRangeCallback,
                        // 智能推荐工具
                        getRecommendationsCallback,
                        getBudgetFriendlyCallback,
                        // 购物车工具
                        addToCartCallback,
                        removeFromCartCallback,
                        getCartCallback,
                        clearCartCallback,
                        getCartCountCallback
                )
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
