package com.suda.controller.admin;


import com.suda.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/shop")
@Api(tags = "管理员店铺相关接口")
public class adminShopController {

    public static final String KEY="SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 设置营业状态
     * @param status 营业状态
     *
     */
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置营业状态为：{}", status == 1 ? "营业中" : "打样中");
        //使用redis来保存营业状态
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    /**
     * 获取营业状态
     * @return 营业状态
     */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus() {
        log.info("获取到营业状态");
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("营业状态为：{}", status == 1 ? "营业中" : "打样中");
        return Result.success(status);
    }
}
