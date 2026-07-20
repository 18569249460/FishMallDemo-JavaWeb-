package com.fishshop.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建订单请求参数，包含本次结算的购物车项和收货信息。
 */
@Data
public class OrderCreateRequest {
    @NotEmpty(message = "请选择要结算的商品")
    private List<Integer> cartItemIds;

    @Valid
    @NotNull(message = "请填写收货信息")
    private ReceiverInfo receiver;
}
