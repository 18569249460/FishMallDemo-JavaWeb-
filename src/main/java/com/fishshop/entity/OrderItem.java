package com.fishshop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单商品快照实体，对应 order_item 表。
 */
@Data
public class OrderItem {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
}
