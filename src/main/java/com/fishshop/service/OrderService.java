package com.fishshop.service;

import com.fishshop.dto.OrderCreateRequest;
import com.fishshop.entity.Order;

import java.util.List;

/**
 * 订单业务接口。
 *
 * <p>订单创建必须放在事务中，确保订单、订单商品、库存、销量和购物车清理一起成功或一起回滚。</p>
 */
public interface OrderService {
    /**
     * 用户提交订单。
     */
    Order createOrder(Integer userId, OrderCreateRequest request);

    /**
     * 用户查看自己的订单列表。
     */
    List<Order> listMyOrders(Integer userId);

    /**
     * 用户查看自己的订单详情。
     */
    Order getMyOrder(Integer userId, Integer orderId);

    /**
     * 用户确认收货。
     */
    void confirmReceipt(Integer userId, Integer orderId);

    /**
     * 用户取消待发货订单。
     */
    void cancel(Integer userId, Integer orderId);

    /**
     * 管理端订单列表。
     */
    List<Order> listAdminOrders(Integer status);

    /**
     * 管理端订单详情。
     */
    Order getAdminOrder(Integer orderId);

    /**
     * 管理员发货。
     */
    void ship(Integer orderId, String trackingNo);
}
