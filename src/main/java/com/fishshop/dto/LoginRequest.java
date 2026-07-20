package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 登录请求参数。
 */
@Data
public class LoginRequest {
    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机格式")
    private String phone;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 32, message = "密码长度必须为 6 到 32 位")
    private String password;
}
