package com.fishshop.dto;

import lombok.Data;

/**
 * 商品列表查询参数。
 */
@Data
public class ProductQuery {
    private Integer categoryId;
    private String keyword;
    private Boolean includeInactive;
}
