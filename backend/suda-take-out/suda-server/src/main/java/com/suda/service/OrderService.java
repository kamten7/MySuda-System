package com.suda.service;


import com.suda.dto.*;
import com.suda.result.PageResult;
import com.suda.vo.OrderPaymentVO;
import com.suda.vo.OrderStatisticsVO;
import com.suda.vo.OrderSubmitVO;
import com.suda.vo.OrderVO;
import org.springframework.stereotype.Service;

public interface OrderService {
    /**
     * 用户下单
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 分页查询订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQueryUser(Integer page, Integer pageSize, Integer status);

    /**
     * 根据id订单详情
     * @param id
     * @return
     */
    OrderVO details(Long id);

    /**
     * 取消订单
     * @param id
     */
    void cancel(Long id) throws Exception;

    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);

    /**
     * 管理员端的订单的条件搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 订单确认
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);


    /**
     * 订单拒接
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 管理员取消订单
     * @param ordersCancelDTO
     */
    void cancelbyAdmin(OrdersCancelDTO ordersCancelDTO) throws  Exception;

    /**
     * 派送订单
     * 管理员端
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单
     *
     * @param id
     */
    void complete(Long id);


    /**
     * 催单
     * @param id
     */
    void reminder(Long id);
}
