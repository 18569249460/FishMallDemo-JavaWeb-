package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.OrderCreateRequest;
import com.fishshop.entity.Order;
import com.fishshop.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户订单接口。
 *
 * <p>负责提交订单、查看个人订单和确认收货，所有操作都以当前登录用户为边界。</p>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 提交订单；Service 中需要开启事务，完成库存校验、扣减库存、生成订单和清理购物车。
     */
    @PostMapping
    public ApiResponse<Order> create(HttpServletRequest servletRequest,
                                     @Valid @RequestBody OrderCreateRequest request) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        return ApiResponse.success(orderService.createOrder(userId, request));
    }

    /**
     * 查询当前用户的订单列表。
     */
    @GetMapping("/my")
    public ApiResponse<List<Order>> myOrders(HttpServletRequest servletRequest) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        return ApiResponse.success(orderService.listMyOrders(userId));
    }

    /**
     * 查询当前用户自己的订单详情，不能越权查看他人订单。
     */
    @GetMapping("/{id}")
    public ApiResponse<Order> detail(HttpServletRequest servletRequest, @PathVariable Integer id) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        return ApiResponse.success(orderService.getMyOrder(userId, id));
    }

    /**
     * 用户确认收货，只允许将“待收货”订单变为“已完成”。
     */
    @PutMapping("/{id}/confirm")
    public ApiResponse<Void> confirm(HttpServletRequest servletRequest, @PathVariable Integer id) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        orderService.confirmReceipt(userId, id);
        return ApiResponse.success(null);
    }

    /**
     * 用户取消待发货订单，取消后后端会回滚库存和销量。
     */
    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(HttpServletRequest servletRequest, @PathVariable Integer id) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        orderService.cancel(userId, id);
        return ApiResponse.success(null);
    }
}
