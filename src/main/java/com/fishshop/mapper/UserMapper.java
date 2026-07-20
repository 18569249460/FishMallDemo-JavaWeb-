package com.fishshop.mapper;

import com.fishshop.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 Mapper。
 */
public interface UserMapper {
    User selectById(@Param("id") Integer id);

    User selectByPhone(@Param("phone") String phone);

    List<User> selectAll();

    int insert(User user);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int updateProfile(User user);

    long countAll();
}
