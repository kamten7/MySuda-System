package com.suda.controller.user;

import com.suda.dto.ShoppingCartDTO;
import com.suda.entity.ShoppingCart;
import com.suda.result.Result;
import com.suda.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Tag(name = "C端-购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @Operation(summary = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车，商品信息为：{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);

        return Result.success();
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "查看购物车")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车");
        List<ShoppingCart> list=shoppingCartService.showShoppingCart();
        return Result.success(list);
    }
    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    @Operation(summary = "清空购物车")
    public Result clean(){
        log.info("清空购物车");
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    /**
     * 删除购物车中单个商品
     */
    @PostMapping("/sub")
    @Operation(summary = "修改购物车")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("修改购物车，商品信息为：{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
