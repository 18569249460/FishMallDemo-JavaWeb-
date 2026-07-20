package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 修改购物车数量请求参数。
 */
@Data
public class CartUpdateRequest {
    @NotNull(message = "请输入购买数量")
    @Min(value = 1, message = "购买数量必须大于 0")
    private Integer quantity;
}
