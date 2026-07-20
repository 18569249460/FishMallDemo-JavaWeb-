package com.fishshop.mapper;

import com.fishshop.entity.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车表 Mapper。
 */
public interface CartMapper {
    Cart selectById(@Param("id") Integer id);

    Cart selectByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    List<Cart> selectByUserId(@Param("userId") Integer userId);

    List<Cart> selectByIds(@Param("userId") Integer userId, @Param("ids") List<Integer> ids);

    int insert(Cart cart);

    int updateQuantity(@Param("id") Integer id, @Param("userId") Integer userId, @Param("quantity") Integer quantity);

    int deleteById(@Param("id") Integer id, @Param("userId") Integer userId);

    int deleteByIds(@Param("userId") Integer userId, @Param("ids") List<Integer> ids);

    int deleteByProductId(@Param("productId") Integer productId);
}
