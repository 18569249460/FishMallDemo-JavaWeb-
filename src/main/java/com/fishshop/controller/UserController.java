package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.UserDTO;
import com.fishshop.dto.UserUpdateRequest;
import com.fishshop.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户个人信息接口。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 查询当前登录用户信息，用户 ID 由 AuthInterceptor 从 Token 中解析后写入 request。
     */
    @GetMapping("/me")
    public ApiResponse<UserDTO> me(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return ApiResponse.success(userService.getCurrentUser(userId));
    }

    /**
     * 修改当前用户昵称和头像。
     */
    @PutMapping("/me")
    public ApiResponse<UserDTO> updateMe(HttpServletRequest request,
                                         @Valid @RequestBody UserUpdateRequest updateRequest) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return ApiResponse.success(userService.updateCurrentUser(userId, updateRequest));
    }
}
