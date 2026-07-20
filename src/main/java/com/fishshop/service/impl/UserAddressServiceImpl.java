package com.fishshop.service.impl;

import com.fishshop.common.BusinessException;
import com.fishshop.common.ResultCode;
import com.fishshop.dto.UserAddressSaveRequest;
import com.fishshop.entity.UserAddress;
import com.fishshop.mapper.UserAddressMapper;
import com.fishshop.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户常用收货地址业务实现。
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {
    private final UserAddressMapper userAddressMapper;

    public UserAddressServiceImpl(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }

    @Override
    public List<UserAddress> list(Integer userId) {
        return userAddressMapper.selectByUserId(userId);
    }

    /**
     * 新增默认地址时，先清空旧默认地址，保证每个用户只有一个默认地址。
     */
    @Transactional
    @Override
    public UserAddress create(Integer userId, UserAddressSaveRequest request) {
        UserAddress address = buildAddress(userId, null, request);
        if (address.getIsDefault() == 1) {
            userAddressMapper.clearDefault(userId);
        }
        userAddressMapper.insert(address);
        return userAddressMapper.selectById(address.getId());
    }

    @Transactional
    @Override
    public UserAddress update(Integer userId, Integer id, UserAddressSaveRequest request) {
        ensureOwned(userId, id);
        UserAddress address = buildAddress(userId, id, request);
        if (address.getIsDefault() == 1) {
            userAddressMapper.clearDefault(userId);
        }
        userAddressMapper.update(address);
        return userAddressMapper.selectById(id);
    }

    @Override
    public void delete(Integer userId, Integer id) {
        int deleted = userAddressMapper.deleteById(id, userId);
        if (deleted == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "收货地址不存在");
        }
    }

    @Transactional
    @Override
    public UserAddress setDefault(Integer userId, Integer id) {
        ensureOwned(userId, id);
        userAddressMapper.clearDefault(userId);
        userAddressMapper.setDefault(id, userId);
        return userAddressMapper.selectById(id);
    }

    private UserAddress buildAddress(Integer userId, Integer id, UserAddressSaveRequest request) {
        UserAddress address = new UserAddress();
        address.setId(id);
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName().trim());
        address.setReceiverPhone(request.getReceiverPhone().trim());
        address.setAddressDetail(request.getAddressDetail().trim());
        address.setIsDefault(Boolean.TRUE.equals(request.getDefaultAddress()) ? 1 : 0);
        return address;
    }

    private void ensureOwned(Integer userId, Integer id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "收货地址不存在");
        }
    }
}
