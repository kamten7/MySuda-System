package com.suda.service;

import com.suda.dto.ShoppingCartDTO;
import com.suda.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 购物车列表
     * @return
     */
    List<ShoppingCart> showShoppingCart();


    /**
     * 清空购物车
     */
    void cleanShoppingCart();

    /**
     * 减购物车
     * @param shoppingCartDTO
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
