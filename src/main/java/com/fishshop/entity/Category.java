package com.fishshop.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体，对应 category 表。
 */
@Data
public class Category {
    private Integer id;
    private String name;
    private Integer sortOrder;
    private Integer status;
    private Long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
