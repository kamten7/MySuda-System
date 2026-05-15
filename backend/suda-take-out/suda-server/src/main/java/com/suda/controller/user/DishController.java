package com.suda.controller.user;

import com.suda.constant.StatusConstant;
import com.suda.entity.Dish;
import com.suda.result.Result;
import com.suda.service.DishService;
import com.suda.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        //构造redis中的key，为dish_id
        String key = "dish_" + categoryId;
        //查询redis中是否存在菜品数据
        List<DishVO> list1= (List<DishVO>) redisTemplate.opsForValue().get(key);
        //存在就直接访问
        if (list1 != null && list1.size() > 0) {
            return Result.success(list1);
        }
        //不存在就查询数据库，将查询结果缓存到redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        list1 = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, list1);
        return Result.success(list1);
    }


}
