package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.OrderShipRequest;
import com.fishshop.entity.Order;
import com.fishshop.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import javax.validation.Valid;

/**
 * 管理端订单处理接口。
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 查询全部订单，可按订单状态筛选。
     */
    @GetMapping
    public ApiResponse<List<Order>> list(@RequestParam(required = false) Integer status) {
        return ApiResponse.success(orderService.listAdminOrders(status));
    }

    /**
     * 查询订单详情，用于管理员发货前核对收货信息和商品明细。
     */
    @GetMapping("/{id}")
    public ApiResponse<Order> detail(@PathVariable Integer id) {
        return ApiResponse.success(orderService.getAdminOrder(id));
    }

    /**
     * 管理员发货，只允许将“待发货”订单变为“待收货”。
     */
    @PutMapping("/{id}/ship")
    public ApiResponse<Void> ship(@PathVariable Integer id,
                                  @Valid @RequestBody(required = false) OrderShipRequest request) {
        orderService.ship(id, request == null ? null : request.getTrackingNo());
        return ApiResponse.success(null);
    }
}
