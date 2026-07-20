package com.fishshop.service;

import com.fishshop.dto.CartAddRequest;
import com.fishshop.dto.CartUpdateRequest;
import com.fishshop.entity.Cart;

import java.util.List;

/**
 * 购物车业务接口。
 */
public interface CartService {
    /**
     * 查询当前用户购物车。
     */
    List<Cart> listItems(Integer userId);

    /**
     * 添加购物车项，需校验商品存在和库存充足。
     */
    Cart addItem(Integer userId, CartAddRequest request);

    /**
     * 修改购物车数量。
     */
    Cart updateItem(Integer userId, Integer cartItemId, CartUpdateRequest request);

    /**
     * 删除购物车项。
     */
    void deleteItem(Integer userId, Integer cartItemId);

    /**
     * 批量删除购物车项。
     */
    void deleteItems(Integer userId, List<Integer> cartItemIds);
}
