package com.fishshop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体，对应 cart 表，并携带联表查询出的商品展示字段。
 */
@Data
public class Cart {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private String productName;
    private BigDecimal productPrice;
    private Integer productStock;
    private Integer productStatus;
    private String productImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
