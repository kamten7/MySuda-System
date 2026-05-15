package com.suda.mapper;


import com.suda.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    /**
     * 查询购物车表
     * @param shoppingCart
     * @return
     */
    //使用动态条件查询购物车数据，所以这里使用shoppingCart
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 修改购物车
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);


    /**
     * 插入购物车
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 根据id删除
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
    /**
     *批量插入购物车数据
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
