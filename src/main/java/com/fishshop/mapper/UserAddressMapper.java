package com.fishshop.mapper;

import com.fishshop.entity.UserAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户常用收货地址 Mapper。
 */
public interface UserAddressMapper {
    UserAddress selectById(@Param("id") Integer id);

    List<UserAddress> selectByUserId(@Param("userId") Integer userId);

    int insert(UserAddress address);

    int update(UserAddress address);

    int deleteById(@Param("id") Integer id, @Param("userId") Integer userId);

    int clearDefault(@Param("userId") Integer userId);

    int setDefault(@Param("id") Integer id, @Param("userId") Integer userId);
}
