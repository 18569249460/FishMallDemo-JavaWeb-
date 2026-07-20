package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 订单收货信息。
 */
@Data
public class ReceiverInfo {
    @NotBlank(message = "请输入收货人姓名")
    @Size(max = 50, message = "收货人姓名不能超过 50 个字符")
    private String receiverName;

    @NotBlank(message = "请输入收货联系电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机格式")
    private String receiverPhone;

    @NotBlank(message = "请输入详细收货地址")
    @Size(max = 255, message = "详细收货地址不能超过 255 个字符")
    private String addressDetail;
}
