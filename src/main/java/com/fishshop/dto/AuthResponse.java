package com.fishshop.dto;

import lombok.Data;

/**
 * 登录或注册成功后的响应数据。
 */
@Data
public class AuthResponse {
    private String token;
    private UserDTO user;
}
