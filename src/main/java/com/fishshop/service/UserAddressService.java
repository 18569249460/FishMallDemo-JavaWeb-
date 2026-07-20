package com.fishshop.service;

import com.fishshop.dto.UserAddressSaveRequest;
import com.fishshop.entity.UserAddress;

import java.util.List;

/**
 * 用户常用地址业务接口。
 */
public interface UserAddressService {
    List<UserAddress> list(Integer userId);

    UserAddress create(Integer userId, UserAddressSaveRequest request);

    UserAddress update(Integer userId, Integer id, UserAddressSaveRequest request);

    void delete(Integer userId, Integer id);

    UserAddress setDefault(Integer userId, Integer id);
}
