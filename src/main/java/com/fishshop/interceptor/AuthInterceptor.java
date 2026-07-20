package com.fishshop.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishshop.common.ApiResponse;
import com.fishshop.common.ResultCode;
import com.fishshop.common.UserRole;
import com.fishshop.common.UserStatus;
import com.fishshop.entity.User;
import com.fishshop.mapper.UserMapper;
import com.fishshop.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录与管理员权限拦截器。
 *
 * <p>普通受保护接口校验 Token、用户存在性和用户状态；/api/admin/** 接口额外校验管理员角色。</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    public AuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper, UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
    }

    /**
     * 在 Controller 执行前解析 Authorization 请求头，并把当前用户信息写入 request。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.UNAUTHORIZED);
            return false;
        }

        JwtUtil.JwtPayload payload;
        try {
            payload = jwtUtil.parse(token);
        } catch (RuntimeException ex) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.UNAUTHORIZED);
            return false;
        }

        User user = userMapper.selectById(payload.getUserId());
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.UNAUTHORIZED);
            return false;
        }
        if (user.getStatus() == null || user.getStatus() != UserStatus.ENABLED.getCode()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.FORBIDDEN);
            return false;
        }

        if (request.getRequestURI().contains("/api/admin/")
                && (user.getRole() == null || user.getRole() != UserRole.ADMIN.getCode())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.FORBIDDEN);
            return false;
        }

        request.setAttribute("currentUserId", user.getId());
        request.setAttribute("currentUserRole", user.getRole());
        return true;
    }

    /**
     * 支持标准的 Bearer Token，也兼容直接传 token 字符串。
     */
    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.trim().isEmpty()) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    /**
     * 拦截失败时直接返回统一 JSON，前端可以按 code 判断跳转登录或提示无权限。
     */
    private void writeError(HttpServletResponse response, int httpStatus, ResultCode resultCode) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(resultCode));
    }
}
