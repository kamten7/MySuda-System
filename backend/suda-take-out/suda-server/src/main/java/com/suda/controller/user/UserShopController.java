package com.suda.controller.user;


import com.suda.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/shop")
@Tag(name = "用户店铺相关接口")
public class UserShopController{

    public static final String KEY="SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取营业状态
     * @return 营业状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取营业状态")
    public Result<Integer> getStatus() {
        log.info("获取到营业状态");
        Integer status = null;
        try {
            status = (Integer) redisTemplate.opsForValue().get(KEY);
        } catch (Exception e) {
            log.warn("Redis读取营业状态失败，使用默认值", e);
        }
        if (status == null) {
            status = 1;
        }
        log.info("营业状态为：{}", status == 1 ? "营业中" : "打样中");
        return Result.success(status);
    }
}
