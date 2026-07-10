package com.suda.AI.tools;

import com.suda.dto.ShoppingCartDTO;
import com.suda.entity.ShoppingCart;
import com.suda.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 购物车工具 —— 供 AI 助手在用户明确确认后操作购物车。
 *
 * <p>方法通过 {@link org.springframework.ai.model.function.FunctionCallback} 注册到 ChatClient。
 * 实际调用由 System Prompt 中的规则约束，确保只有用户确认后才操作。</p>
 */
@Component
@RequiredArgsConstructor
public class CartTools {

    private final ShoppingCartService shoppingCartService;

    /**
     * 将菜品/套餐添加到购物车。
     */
    public String addToCart(Long dishId, Long setmealId, String dishFlavor, Integer number) {
        if (dishId == null && setmealId == null) {
            return "请提供要添加的菜品ID或套餐ID";
        }

        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(dishId);
        dto.setSetmealId(setmealId);
        dto.setDishFlavor(dishFlavor);

        int count = number != null ? number : 1;
        for (int i = 0; i < count; i++) {
            shoppingCartService.addShoppingCart(dto);
        }

        String flavorNote = (dishFlavor != null && !dishFlavor.isEmpty())
                ? "，口味：" + dishFlavor : "";
        return String.format("✅ 已添加到购物车，数量：%d%s", count, flavorNote);
    }

    /**
     * 从购物车减少或移除指定菜品/套餐。
     */
    public String removeFromCart(Long dishId, Long setmealId) {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(dishId);
        dto.setSetmealId(setmealId);

        shoppingCartService.subShoppingCart(dto);
        return "已从购物车移除一份";
    }

    /**
     * 查看购物车内容。
     */
    public String getCart() {
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
        shoppingCartService.cleanShoppingCart();
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
