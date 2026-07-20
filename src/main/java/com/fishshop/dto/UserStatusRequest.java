package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端启用或禁用用户的请求参数。
 */
@Data
public class UserStatusRequest {
    @NotNull(message = "请选择用户状态")
    private Integer status;
}
