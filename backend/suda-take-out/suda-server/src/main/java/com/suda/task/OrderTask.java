package com.suda.task;


import com.suda.entity.Orders;
import com.suda.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron="0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("处理超时订单:{}", LocalDateTime.now());
        //select * from order where status = 1 and order_time < 当前时间-15分钟 ---->超时
        //获取待支付订单
        List<Orders> orderList=orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(orderList!=null && orderList.size()>0){
            for(Orders orders:orderList){
                orders.setStatus(Orders.CANCELLED);//设置成取消
                orders.setCancelReason("支付超时，订单取消");
                orders.setCancelTime(LocalDateTime.now());//设置取消时间
                orderMapper.update(orders);//更新订单状态
            }
        }

    }

    /**
     * 处理一直处于派送订单
     */
    @Scheduled(cron="0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理处于派送状态的订单:{}", LocalDateTime.now());
        //select * from order where status = 4 and order_time < 当前时间-1小时
        //Orders.DELIVERY_IN_PROGRESS--->派送中-->值为4
        List<Orders> orderList=orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusHours(-1));

        if(orderList!=null && orderList.size()>0){
            for(Orders orders:orderList){
                orders.setStatus(Orders.COMPLETED);//设置成已完成
                orders.setCancelReason("派送超时，订单完成");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);//更新订单状态
            }
        }
    }
}
