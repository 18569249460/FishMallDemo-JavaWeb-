package com.fishshop.service.impl;

import com.fishshop.common.BusinessException;
import com.fishshop.common.ResultCode;
import com.fishshop.common.UserRole;
import com.fishshop.common.UserStatus;
import com.fishshop.dto.AuthResponse;
import com.fishshop.dto.LoginRequest;
import com.fishshop.dto.RegisterRequest;
import com.fishshop.entity.User;
import com.fishshop.mapper.UserMapper;
import com.fishshop.service.AuthService;
import com.fishshop.util.JwtUtil;
import com.fishshop.util.PasswordUtil;
import com.fishshop.util.UserDtoConverter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 认证业务实现。
 *
 * <p>负责注册、登录、明文密码校验、JWT 签发和返回前端可见用户信息。</p>
 */
@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 登录：先按手机号查用户，再校验密码和账号状态，最后签发 JWT。
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectByPhone(request.getPhone());
        if (user == null || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "手机号或密码错误");
        }
        ensureUserEnabled(user);
        return buildAuthResponse(user);
    }

    /**
     * 注册：手机号唯一，密码按当前项目要求明文入库，默认创建普通启用用户。
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.selectByPhone(request.getPhone()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "手机号已注册");
        }

        User user = new User();
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setRole(UserRole.USER.getCode());
        user.setStatus(UserStatus.ENABLED.getCode());

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "手机号已注册");
        }

        User savedUser = userMapper.selectById(user.getId());
        return buildAuthResponse(savedUser == null ? user : savedUser);
    }

    /**
     * 禁用用户不能登录。
     */
    private void ensureUserEnabled(User user) {
        if (user.getStatus() == null || user.getStatus() != UserStatus.ENABLED.getCode()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用");
        }
    }

    /**
     * 组装登录/注册响应，Token 中只保存用户 ID 和角色。
     */
    private AuthResponse buildAuthResponse(User user) {
        AuthResponse response = new AuthResponse();
        response.setToken(jwtUtil.generateToken(user.getId(), user.getRole()));
        response.setUser(UserDtoConverter.toUserDTO(user));
        return response;
    }
}
