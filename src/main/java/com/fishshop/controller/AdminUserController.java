package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.AdminCreateRequest;
import com.fishshop.dto.UserDTO;
import com.fishshop.dto.UserStatusRequest;
import com.fishshop.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端用户管理接口。
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 查询注册用户列表。
     */
    @GetMapping
    public ApiResponse<List<UserDTO>> list() {
        return ApiResponse.success(userService.listUsers());
    }

    /**
     * 新增管理员账号。
     *
     * <p>该接口位于 /api/admin/** 下，只有已登录管理员能调用；普通注册入口不能创建管理员。</p>
     */
    @PostMapping("/admins")
    public ApiResponse<UserDTO> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        return ApiResponse.success(userService.createAdmin(request));
    }

    /**
     * 启用或禁用普通用户；管理员账号不允许被普通操作误禁用。
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Integer id,
                                          @Valid @RequestBody UserStatusRequest request) {
        userService.updateStatus(id, request.getStatus());
        return ApiResponse.success(null);
    }
}
