package com.fishshop.service;

import com.fishshop.dto.AuthResponse;
import com.fishshop.dto.LoginRequest;
import com.fishshop.dto.RegisterRequest;

/**
 * 认证业务接口，封装登录、注册和 Token 生成相关流程。
 */
public interface AuthService {
    /**
     * 登录：校验手机号、密码、用户状态，通过后返回 Token 和用户信息。
     */
    AuthResponse login(LoginRequest request);

    /**
     * 注册：校验手机号唯一性，保存明文密码并创建普通用户。
     */
    AuthResponse register(RegisterRequest request);
}
