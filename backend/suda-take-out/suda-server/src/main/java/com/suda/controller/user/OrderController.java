package com.suda.controller.user;


import com.suda.dto.OrdersPaymentDTO;
import com.suda.dto.OrdersSubmitDTO;
import com.suda.result.PageResult;
import com.suda.result.Result;
import com.suda.service.OrderService;
import com.suda.vo.OrderPaymentVO;
import com.suda.vo.OrderSubmitVO;
import com.suda.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单参数为：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */

    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        //调用订单支付接口，并返回支付的参数orderPaymentVO
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        //这行代码的作用： 后端会拿着订单号去微信服务器换取一个“预支付交易单”（Prepay ID），并进行一系列复杂的加密签名

        log.info("生成预支付交易单：{}", orderPaymentVO);
        //只要是进来的，都认为支付成功
        // 模拟支付成功
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     * 分页查询
     * @param page 订单当前页面
     * @param pageSize 每页显示多少条数据
     * @param status 订单状态
     * @return 订单列表
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> page(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Integer status){
        log.info("分页查询历史订单，页码：{}，页大小：{}，订单状态：{}", page, pageSize, status);
        PageResult pageResult=orderService.pageQueryUser(page,pageSize,status);
        return Result.success(pageResult);
    }

    /**
     * 订单详情
     *根据订单id获取订单的详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> orderDetail(@PathVariable("id") Long id){
        log.info("查询订单详情：{}", id);
        OrderVO orderVO = new OrderVO();
        orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable("id") Long id) throws Exception{
        log.info("取消订单：{}", id);
        orderService.cancel(id);
        return Result.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable("id") Long id){
        log.info("再来一单：{}", id);
        orderService.repetition(id);

        return Result.success();
    }


    /**
     * 客户催单
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("客户催单")
    public Result reminder(@PathVariable("id") Long id){
        orderService.reminder(id);
        return Result.success();
    }
















}
