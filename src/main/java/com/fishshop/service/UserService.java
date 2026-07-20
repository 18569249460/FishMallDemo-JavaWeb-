package com.fishshop.service;

import com.fishshop.dto.AdminCreateRequest;
import com.fishshop.dto.UserDTO;
import com.fishshop.dto.UserUpdateRequest;

import java.util.List;

/**
 * 用户业务接口，包含当前用户信息和管理端用户状态维护。
 */
public interface UserService {
    /**
     * 获取当前登录用户信息，返回时不能包含密码。
     */
    UserDTO getCurrentUser(Integer userId);

    /**
     * 当前用户修改昵称和头像。
     */
    UserDTO updateCurrentUser(Integer userId, UserUpdateRequest request);

    /**
     * 管理端用户列表。
     */
    List<UserDTO> listUsers();

    /**
     * 管理端新增管理员账号。
     */
    UserDTO createAdmin(AdminCreateRequest request);

    /**
     * 管理端启用或禁用普通用户。
     */
    void updateStatus(Integer userId, Integer status);
}
