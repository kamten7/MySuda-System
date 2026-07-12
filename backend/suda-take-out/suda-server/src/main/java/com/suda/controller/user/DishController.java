package com.suda.controller.user;

import com.suda.constant.StatusConstant;
import com.suda.entity.Dish;
import com.suda.result.Result;
import com.suda.service.DishService;
import com.suda.vo.DishVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "C端-菜品浏览接口")
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
    @Operation(summary = "根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        String key = "dish_" + categoryId;
        try {
            List<DishVO> list1 = (List<DishVO>) redisTemplate.opsForValue().get(key);
            if (list1 != null && !list1.isEmpty()) {
                return Result.success(list1);
            }
        } catch (Exception e) {
            log.warn("Redis查询失败，降级走数据库: key={}", key, e);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        List<DishVO> list1 = dishService.listWithFlavor(dish);

        try {
            redisTemplate.opsForValue().set(key, list1, 30, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis缓存写入失败: key={}", key, e);
        }

        return Result.success(list1);
    }


}
