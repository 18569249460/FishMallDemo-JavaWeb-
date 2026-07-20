package com.fishshop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体，对应 product 表。
 */
@Data
public class Product {
    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String specification;
    private String imageUrl;
    private String description;
    private String origin;
    private String shelfLife;
    private String storageMethod;
    private String deliveryInfo;
    private Integer sales;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
