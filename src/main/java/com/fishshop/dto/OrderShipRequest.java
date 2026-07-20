package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 管理员发货请求参数。
 */
@Data
public class OrderShipRequest {
    @Size(max = 100, message = "物流单号不能超过 100 个字符")
    private String trackingNo;
}
