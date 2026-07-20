package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.AuthResponse;
import com.fishshop.dto.LoginRequest;
import com.fishshop.dto.RegisterRequest;
import com.fishshop.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证接口：负责登录和注册。
 *
 * <p>登录成功后由 Service 返回 Token 与用户基础信息。</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 手机号 + 密码登录。
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    /**
     * 注册普通用户，后续实现时可注册成功后自动登录并返回 Token。
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }
}
