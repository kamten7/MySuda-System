package com.suda.AI.tools;

import com.suda.dto.ShoppingCartDTO;
import com.suda.entity.ShoppingCart;
import com.suda.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 购物车工具 —— 纯 Java 工具类，由 {@code UserAIIntentService} 直接调用操作购物车。
 *
 * <p>使用 ThreadLocal 追踪当前请求中购物车是否被修改，供上层服务判断 cartUpdated。</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartTools {

    private final ShoppingCartService shoppingCartService;

    /** 请求级别的购物车变更标记 */
    private static final ThreadLocal<Boolean> CART_MODIFIED = ThreadLocal.withInitial(() -> false);

    /**
     * 当前 AI 请求的用户 ID（由 UserAIServiceImpl 在 streamChat 前设置）。
     * 解决 Function Calling 在 reactor 线程执行时 BaseContext ThreadLocal 为 null 的问题。
     * 设为 volatile 保证多线程可见性。
     */
    private volatile Long currentUserId;

    /** 检查当前请求中购物车是否被修改，并在读取后重置 */
    public static boolean isCartModifiedAndReset() {
        Boolean modified = CART_MODIFIED.get();
        CART_MODIFIED.remove();
        return Boolean.TRUE.equals(modified);
    }

    /** 由 UserAIServiceImpl 在 AI 调用前设置 */
    public void setCurrentUserId(Long userId) { this.currentUserId = userId; }

    /**
     * 将菜品/套餐添加到购物车。
     */
    public String addToCart(Long dishId, Long setmealId, String dishFlavor, Integer number) {
        log.info("🛒 AI调用 addToCart: dishId={}, setmealId={}, dishFlavor={}, number={}, userId={}",
                dishId, setmealId, dishFlavor, number, currentUserId);

        if (dishId == null && setmealId == null) {
            log.warn("addToCart 参数无效：dishId和setmealId均为null");
            return "请提供要添加的菜品ID或套餐ID";
        }

        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(dishId);
        dto.setSetmealId(setmealId);
        dto.setDishFlavor(dishFlavor);
        dto.setUserId(currentUserId);  // 通过 DTO 传递 userId，解决跨线程 ThreadLocal 失效

        int count = number != null ? number : 1;
        for (int i = 0; i < count; i++) {
            shoppingCartService.addShoppingCart(dto);
        }

        String flavorNote = (dishFlavor != null && !dishFlavor.isEmpty())
                ? "，口味：" + dishFlavor : "";
        CART_MODIFIED.set(true);
        String result = String.format("✅ 已添加到购物车，数量：%d%s", count, flavorNote);
        log.info("addToCart 完成: {}", result);
        return result;
    }

    /**
     * 从购物车减少或移除指定菜品/套餐。
     */
    public String removeFromCart(Long dishId, Long setmealId) {
        log.info("🛒 AI调用 removeFromCart: dishId={}, setmealId={}", dishId, setmealId);
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(dishId);
        dto.setSetmealId(setmealId);

        shoppingCartService.subShoppingCart(dto);
        CART_MODIFIED.set(true);
        return "已从购物车移除一份";
    }

    /**
     * 查看购物车内容。
     */
    public String getCart() {
        log.info("🛒 AI调用 getCart");
        List<ShoppingCart> cartList = shoppingCartService.showShoppingCart();

        if (cartList == null || cartList.isEmpty()) {
            return "🛒 您的购物车是空的，去逛逛吧~";
        }

        StringBuilder result = new StringBuilder("🛒 您的购物车：\n");
        double total = 0;
        int totalCount = 0;

        for (ShoppingCart item : cartList) {
            double itemTotal = item.getAmount().doubleValue() * item.getNumber();
            total += itemTotal;
            totalCount += item.getNumber();

            String flavorNote = (item.getDishFlavor() != null && !item.getDishFlavor().isEmpty())
                    ? "（" + item.getDishFlavor() + "）" : "";

            result.append(String.format("- %s%s × %d = ¥%.2f\n",
                    item.getName(), flavorNote, item.getNumber(), itemTotal));
        }

        result.append(String.format("\n合计：%d 件商品，共 ¥%.2f", totalCount, total));
        return result.toString();
    }

    /**
     * 清空购物车。
     */
    public String clearCart() {
        log.info("🛒 AI调用 clearCart");
        shoppingCartService.cleanShoppingCart();
        CART_MODIFIED.set(true);
        return "购物车已清空";
    }

    /**
     * 获取购物车商品总数量。
     */
    public String getCartCount() {
        List<ShoppingCart> cartList = shoppingCartService.showShoppingCart();
        int count = cartList != null
                ? cartList.stream().mapToInt(item -> item.getNumber() != null ? item.getNumber() : 0).sum()
                : 0;
        return String.format("购物车中有 %d 件商品", count);
    }
}
