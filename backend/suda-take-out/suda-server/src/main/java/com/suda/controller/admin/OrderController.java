package com.suda.controller.admin;


import com.suda.dto.OrdersCancelDTO;
import com.suda.dto.OrdersConfirmDTO;
import com.suda.dto.OrdersPageQueryDTO;
import com.suda.dto.OrdersRejectionDTO;
import com.suda.result.PageResult;
import com.suda.result.Result;
import com.suda.service.OrderService;
import com.suda.vo.OrderStatisticsVO;
import com.suda.vo.OrderVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Tag(name = "订单管理接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单的搜索
     * @param ordersPageQueryDTO 前端的搜索参数
     * @return 订单的分页结果
     */
    @GetMapping("/conditionSearch")
    @Operation(summary = "订单的搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){

        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个状态订单的数据统计
     * @return
     */
    @GetMapping("/statistics")
    @Operation(summary = "各个状态订单的数据统计")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @Operation(summary = "查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     *
     * @return
     */
    @PutMapping("/confirm")
    @Operation(summary = "接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     *
     * @return
     */
    @PutMapping("/rejection")
    @Operation(summary = "拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     *
     * @return
     */
    @PutMapping("/cancel")
    @Operation(summary = "取消订单")
    public Result cancelbyAdmin(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.cancelbyAdmin(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     *
     * @return
     */
    @PutMapping("/delivery/{id}")
    @Operation(summary = "派送订单")
    public Result delivery(@PathVariable("id") Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     *
     * @return
     */
    @PutMapping("/complete/{id}")
    @Operation(summary = "完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }

}
