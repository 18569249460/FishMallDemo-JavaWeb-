package com.fishshop.service.impl;

import com.fishshop.common.BusinessException;
import com.fishshop.common.ResultCode;
import com.fishshop.common.UserRole;
import com.fishshop.common.UserStatus;
import com.fishshop.dto.AdminCreateRequest;
import com.fishshop.dto.UserDTO;
import com.fishshop.dto.UserUpdateRequest;
import com.fishshop.entity.User;
import com.fishshop.mapper.UserMapper;
import com.fishshop.service.UserService;
import com.fishshop.util.PasswordUtil;
import com.fishshop.util.UserDtoConverter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户业务实现。
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 获取当前登录用户信息。拦截器已做 Token 校验，这里再查库确保用户仍然存在。
     */
    @Override
    public UserDTO getCurrentUser(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户不存在");
        }
        if (user.getStatus() == null || user.getStatus() != UserStatus.ENABLED.getCode()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用");
        }
        return UserDtoConverter.toUserDTO(user);
    }

    /**
     * 当前用户只能修改自己的昵称和头像，手机号与角色状态不在这里变更。
     */
    @Override
    public UserDTO updateCurrentUser(Integer userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户不存在");
        }
        user.setNickname(request.getNickname().trim());
        user.setAvatar(trimToNull(request.getAvatar()));
        userMapper.updateProfile(user);
        return UserDtoConverter.toUserDTO(userMapper.selectById(userId));
    }

    /**
     * 管理端用户列表。
     *
     * <p>返回 DTO 而不是实体，避免 password 字段暴露给前端。</p>
     */
    @Override
    public List<UserDTO> listUsers() {
        List<User> users = userMapper.selectAll();
        List<UserDTO> result = new ArrayList<UserDTO>();
        for (User user : users) {
            result.add(UserDtoConverter.toUserDTO(user));
        }
        return result;
    }

    /**
     * 管理端新增管理员账号。
     *
     * <p>这里复用 user 表保存管理员，区别只在 role=1；密码按当前项目要求明文保存。</p>
     */
    @Override
    public UserDTO createAdmin(AdminCreateRequest request) {
        String phone = request.getPhone().trim();
        if (userMapper.selectByPhone(phone) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "手机号已注册");
        }

        User admin = new User();
        admin.setNickname(request.getNickname().trim());
        admin.setPhone(phone);
        admin.setPassword(PasswordUtil.encode(request.getPassword()));
        admin.setRole(UserRole.ADMIN.getCode());
        admin.setStatus(UserStatus.ENABLED.getCode());

        try {
            userMapper.insert(admin);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "手机号已注册");
        }

        User savedAdmin = userMapper.selectById(admin.getId());
        return UserDtoConverter.toUserDTO(savedAdmin == null ? admin : savedAdmin);
    }

    /**
     * 管理端启用或禁用普通用户。
     *
     * <p>管理员账号不允许通过页面操作被禁用，避免系统失去后台入口。</p>
     */
    @Override
    public void updateStatus(Integer userId, Integer status) {
        if (status == null
                || (status != UserStatus.ENABLED.getCode() && status != UserStatus.DISABLED.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户状态无效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        if (user.getRole() != null && user.getRole() == UserRole.ADMIN.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "管理员账号不能被禁用");
        }

        int updated = userMapper.updateStatus(userId, status);
        if (updated == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户状态修改失败");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
