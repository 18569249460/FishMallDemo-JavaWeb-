package com.fishshop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体，对应 orders 表，并可携带订单商品明细。
 */
@Data
public class Order {
    private Integer id;
    private String orderNo;
    private Integer userId;
    private String userNickname;
    private String userPhone;
    private String receiverName;
    private String receiverPhone;
    private String addressDetail;
    private String trackingNo;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> items;
}
