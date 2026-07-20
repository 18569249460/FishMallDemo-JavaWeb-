package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.UserAddressSaveRequest;
import com.fishshop.entity.UserAddress;
import com.fishshop.service.UserAddressService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户常用收货地址接口。
 */
@RestController
@RequestMapping("/api/users/addresses")
public class UserAddressController {
    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @GetMapping
    public ApiResponse<List<UserAddress>> list(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return ApiResponse.success(userAddressService.list(userId));
    }

    @PostMapping
    public ApiResponse<UserAddress> create(HttpServletRequest request,
                                           @Valid @RequestBody UserAddressSaveRequest saveRequest) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return ApiResponse.success(userAddressService.create(userId, saveRequest));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserAddress> update(HttpServletRequest request,
                                           @PathVariable Integer id,
                                           @Valid @RequestBody UserAddressSaveRequest saveRequest) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return ApiResponse.success(userAddressService.update(userId, id, saveRequest));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        userAddressService.delete(userId, id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<UserAddress> setDefault(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return ApiResponse.success(userAddressService.setDefault(userId, id));
    }
}
