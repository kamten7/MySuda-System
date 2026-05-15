package com.suda.controller.admin;

import com.suda.dto.DishDTO;
import com.suda.dto.DishPageQueryDTO;
import com.suda.entity.Dish;
import com.suda.result.PageResult;
import com.suda.result.Result;

import com.suda.service.DishService;
import com.suda.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDTO 菜品数据
     * @return 新增结果集
     */
    @PostMapping
    @ApiOperation(value = "新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //删除缓存数据
        String key = "dish_"+dishDTO.getCategoryId();
        cleanCache(key);

        return Result.success();
    }

    /**
     * 菜品管理分页查询
     * @param pageQueryDTO 分页查询参数DTO
     */
    @GetMapping("/page")
    @ApiOperation(value = "菜品管理分页查询")
    public Result<PageResult> page(DishPageQueryDTO pageQueryDTO){
        log.info("分页查询菜品：{}", pageQueryDTO);
        PageResult pageResult =dishService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 批量删除菜品
     * @param ids 菜品id列表
     *
     */
    @DeleteMapping
    @ApiOperation(value = "批量删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        //把所有以dish开头的key都删除

        cleanCache("dish_*");

        return Result.success();
    }
    /**
     * 根据id查询菜品信息
     * @param id 菜品id
     * @return 菜品信息
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询菜品信息")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品信息：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     * @param dishDTO 菜品数据
     * @return 修改结果集
     */
    @PutMapping
    @ApiOperation(value = "修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status, id);

        cleanCache("dish_*");

        return Result.success();

    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }





    /**
     * 清理缓存数据
     * 清理redis中缓存的菜品数据，因为每次添加或修改或删除菜品，都需要更新缓存中的数据
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys=redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }




}
