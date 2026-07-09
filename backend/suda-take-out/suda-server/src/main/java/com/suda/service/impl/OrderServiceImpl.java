package com.suda.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.suda.constant.MessageConstant;
import com.suda.context.BaseContext;
import com.suda.dto.*;
import com.suda.entity.*;
import com.suda.exception.AddressBookBusinessException;
import com.suda.exception.OrderBusinessException;
import com.suda.mapper.*;
import com.suda.result.PageResult;
import com.suda.service.AddressBookService;
import com.suda.service.OrderService;
import com.suda.utils.HttpClientUtil;
import com.suda.utils.WeChatPayUtil;
import com.suda.vo.OrderPaymentVO;
import com.suda.vo.OrderStatisticsVO;
import com.suda.vo.OrderSubmitVO;
import com.suda.vo.OrderVO;
import com.suda.websocket.WebSocketServer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${suda.shop.address}")
    private String shopAddress;

    @Value("${suda.baidu.ak}")
    private String ak;


    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //ordersSubmitDTO中有的属性为：
        //  addressBookId，payMethod，remark，estimatedDeliveryTime，deliveryStatus，tablewareNumber，tablewareStatus，packAmount，amount，orderTime
        /**
         * 1、处理业务异常--地址簿为空，购物车数据为空
         *
         */
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //2、地址簿超出配送范围
        String fullAddress = addressBook.getProvinceName()
                + addressBook.getCityName()
                + addressBook.getDistrictName()
                + addressBook.getDetail();
        checkOutOfRange(fullAddress);

        //查询当前用户的购物车数据，判断是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartlist = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartlist== null || shoppingCartlist.size()== 0) {
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        /**
            * 2、向订单表插入一条数据
            *这里使用order对象来封装订单数据
            *要先将前端传过来的DTO对象进行拷贝赋值到order对象中
            *然后利用order对象来操作数据库，插入数据
         */
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());//下单时间
        orders.setPayStatus(Orders.UN_PAID);//待支付
        orders.setStatus(Orders.PENDING_PAYMENT);//待付款
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//订单号
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());//收货人
        orders.setUserId(userId);

        /**
            * 3、给订单明细表插入多条订单明细数据
            *这里在插入订单数据时，返回了订单id，订单的主键值，使用了 keyProperty="id"
            * keyProperty="id"
            *指定将生成的主键值回填到哪个属性
            *这里是回填到 orders 对象的 id 属性
         */
        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart cart : shoppingCartlist) {
            //将购物车数据转为订单明细数据，一个个的插入
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置当前订单明细关联的订单id
            orderDetailList.add(orderDetail);
        }
        //批量插入订单明细数据
        orderDetailMapper.insertBatch(orderDetailList);

        //4、下单成功后清空该用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        //5、封装VO返回结果
        //VO对象是专门用于封装返回给前端的数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *这里直接跳过微信支付
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        //通过用户的id查找用户的实体类，封装到user对象中
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "速达外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        //直接new一个JSONObject对象，用于模拟微信支付成功，并返回给前端
        JSONObject jsonObject = new JSONObject();
        //模拟微信支付成功
        jsonObject.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        jsonObject.put("nonceStr", "fake_nonce_str");
        jsonObject.put("package", "prepay_id=fake_prepay_id");
        jsonObject.put("signType", "RSA");
        jsonObject.put("paySign", "fake_pay_sign");
        jsonObject.put("code", "SUCCESS");

        //判断该订单是否已经支付过了
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }
        //执行支付操作
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 1. 先去数据库里把这笔订单查出来（此时 ordersDB 里有订单的所有信息）
        // 根据订单号查询订单
        //因为后面需要通过订单的id来查询订单详情
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 2. 创建一个全新的、只包含关键信息的 orders 对象（用于更新）
        //这个对象只包含订单的id、状态、支付方式、支付状态、结账时间，其他都是空的，这样提升了mybatis的性能
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);


        //通过websocket向客户端推送消息
        Map<String, Object> map=new HashMap();
        map.put("type",1);//1表示来单提醒,2表示客户催单
        map.put("orderId", ordersDB.getId());
        map.put("content","订单号:"+outTradeNo);
        //封装成JSON对象
        String json = JSON.toJSONString(map);
        //发送到客户端
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 用户订单分页查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQueryUser(Integer page, Integer pageSize, Integer status) {
        //利用拦截器获取单前的用户id，这里是前端小程序的
        Long userId = BaseContext.getCurrentId();

        //设置分页参数
        PageHelper.startPage(page, pageSize);

        //创建数据库的操作对象
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(userId);
        ordersPageQueryDTO.setStatus(status);

        //执行查询，PageHelper会自动拦截并基于mybatis修改sql
        //这里查回来的是orders对象--->orders对象就是数据库中的orders对象的详细数据，一一对应
        Page<Orders> ordersPage = orderMapper.pageQueryUser(ordersPageQueryDTO);

        //获取查询结果-----如果没有数据就直接返回，也就是没有历史订单
        if(ordersPage==null || ordersPage.size()==0){
            //因为pageResult是由total和records两个属性组成的，所以这里返回一个空的PageResult对象
            //records是Object类型的数组
            return new PageResult(0L, new ArrayList());
        }
        //获取查询结果-----如果有数据就进行封装
        //前面是使用了Orders对象和数据库进行操作
        //和数据库交互完获得的数据需要重新封装成VO对象返回给前端
        List<OrderVO> ordersVOList = new ArrayList<>();

        for (Orders o : ordersPage) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(o,orderVO);


            //接着需要根据订单获取订单的明细，这里需要调用订单明细的接口
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(o.getId());
            //封装到VO对象中
            orderVO.setOrderDetailList(orderDetailList);
            //添加到VO对象列表中
            ordersVOList.add(orderVO);
        }



        return new PageResult(ordersPage.getTotal(),ordersVOList);
    }

    /**
     * 根据订单的id获取订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        //OrderVO=Orders+OrderDetail
        //先获取订单
        Orders orders = orderMapper.getById(id);

        List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) throws Exception{
        //先去查询订单，看是否存在
       Orders orderDB = orderMapper.getById(id);

       if(orderDB==null){
           //订单不存在
           throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
       }
       if(orderDB.getStatus()>2){
           //订单已经完成
           throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
       }
       if(orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
           //待接单
           //支付状态修改为 退款
           orderDB.setPayStatus(Orders.REFUND);
       }
       //创建一个Orders对象，用于更新订单的状态
       Orders orders = new Orders();
       orders.setId(orderDB.getId());
       orders.setStatus(Orders.CANCELLED);
       orders.setCancelReason("用户取消");
       orders.setCancelTime(LocalDateTime.now());
       orderMapper.update(orders);

    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        //查询当前用户id
        Long userId = BaseContext.getCurrentId();
        //根据当前的id将该id对应的用户的订单查出来，并将其中的商品加入购物车
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(id);

        //将订单详情对象转化为购物车对象--->便于购物车接口的调用
        List<ShoppingCart> shoppingCartList=orderDetailList.stream().map(x->{
            ShoppingCart shoppingCart=new ShoppingCart();
            //x-->orderDetail
            BeanUtils.copyProperties(x,shoppingCart,"id");//拷贝除id以外的属性
            shoppingCart.setUserId(userId);//设置用户id
            shoppingCart.setCreateTime(LocalDateTime.now());//设置创建时间
            return shoppingCart;//返回购物车对象
        }).collect(Collectors.toList());
        //执行sql进行插入
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 管理员端的订单条件搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //调用ordermapper下的pageQueryUser方法-->获取分页数据
        Page<Orders> page=orderMapper.pageQueryUser(ordersPageQueryDTO);

        //因为部分的订单状态需要额外返回订单的菜品信息，所以需要转成VO传回去给前端
        List<OrderVO> orderVOList=getOrderVOList(page);

        return new PageResult(page.getTotal(),orderVOList);
    }


    private List<OrderVO> getOrderVOList(Page<Orders> page){
        List<OrderVO> orderVOList=new ArrayList<>();
        //获取查询结果
        List<Orders> ordersList=page.getResult();
        if(!CollectionUtils.isEmpty(ordersList)){
            for(Orders orders:ordersList){
                //遍历并复制
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                //获取订单的菜品信息
                String orderDishes=getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }
    /**
     * 根据订单的id获取菜品信息字符串
     *
     */
    private String getOrderDishesStr(Orders orders){
        //先根据订单的id查询订单菜品详情信息
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orders.getId());
        //将每一条订单菜品信息拼接为字符串
        List< String> orderDishList=orderDetailList.stream().map(x->{
            String orderDish=x.getName()+"*"+x.getNumber()+";";
            return orderDish;
        }).collect(Collectors.toList());
        //将该订单对应的所以菜品信息拼接到一起来
        return String.join("",orderDishList);
    }


    /**
     * 统计订单数据
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 订单确认
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
       Orders orders = Orders.builder()
               .id(ordersConfirmDTO.getId())
               .status(Orders.CONFIRMED)
               .build();
       orderMapper.update(orders);
    }

    /**
     * 订单拒绝
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款

            log.info("申请退款");
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 管理员取消订单
     * @param ordersCancelDTO
     * @throws Exception
     */
    @Override
    public void cancelbyAdmin(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
            //用户已支付，需要退款

            log.info("申请退款");
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        //先查询id是否存在
        Orders orderDB=orderMapper.getById(id);
        if(orderDB==null ){
            //不存在或者已经在配送中的
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //存在
        Map<String,Object> map=new HashMap<>();
        map.put("type",2);//1.标识来单提醒，2.表示客户催单
        map.put("orderId",id);
        map.put("content","订单号:"+orderDB.getNumber());
        String json = JSON.toJSONString(map);
        //发送消息
        webSocketServer.sendToAllClient(json);
    }


    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address 客户收货地址
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");



        //地址日志
        log.info("店铺地址: {}", shopAddress);
        log.info("用户地址: {}", address);
        log.info("配送距离: {} 米", distance);

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }
}
