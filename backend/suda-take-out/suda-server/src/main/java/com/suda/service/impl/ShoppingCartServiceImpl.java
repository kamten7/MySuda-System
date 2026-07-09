package com.suda.service.impl;

import com.suda.context.BaseContext;
import com.suda.dto.ShoppingCartDTO;
import com.suda.entity.Dish;
import com.suda.entity.Setmeal;
import com.suda.entity.ShoppingCart;
import com.suda.exception.BaseException;
import com.suda.mapper.DishMapper;
import com.suda.mapper.SetmealMapper;
import com.suda.mapper.ShoppingCartMapper;
import com.suda.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车：{}", shoppingCartDTO);
        // 在添加之前需要先查询当前菜品或套餐是否在购物车中
        //param:userid dishId/setmealId
        ShoppingCart shoppingCart = new ShoppingCart();
        //先拷贝成shoppingCart对象
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //设置用户id，通过用户端的拦截器取出来
        Long userId =BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //如果存在就修改数量，不存在就添加
        if(list!=null && list.size()>0){
            //这里使用索引0是因为这里最多只能查询到一个匹配的
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);
            //修改
            shoppingCartMapper.updateNumberById(cart);
        }else{
            //不存在就插入购物车
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId!=null){
                //添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                if(dish == null){
                    throw new BaseException("菜品不存在");
                }
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }else{
                //添加的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();

                Setmeal setmeal = setmealMapper.getById(setmealId);
                if(setmeal == null){
                    throw new BaseException("套餐不存在");
                }

                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }

    }

    @Override
    public List<ShoppingCart> showShoppingCart(){

        Long userId =BaseContext.getCurrentId();

        ShoppingCart shoppingCart =ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }


    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        Long userId =BaseContext.getCurrentId();
        //根据用户id删除
        shoppingCartMapper.deleteByUserId(userId);
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId =BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //查询出当前用户的购物车数据，这里的话最多能查出一条对应的数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list!=null && list.size()>0){
            //这里重新赋值
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            if(number == 1){
                //如果数量为1，就删除
                //这里是根据购物车的id删除，直接删除这个购物车
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else{
                //数量减1
                //这里复用之前的更新操作，只不过是减一，但是在mapper层是直接赋值
                shoppingCart.setNumber(number-1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }

    }
}
