package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.CartAddRequest;
import com.fishshop.dto.CartUpdateRequest;
import com.fishshop.dto.IdListRequest;
import com.fishshop.entity.Cart;
import com.fishshop.service.CartService;
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
 * 购物车接口。
 *
 * <p>所有购物车操作都需要登录，并且必须基于当前用户 ID 做数据归属校验。</p>
 */
@RestController
@RequestMapping("/api/cart/items")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 查询当前用户购物车列表。
     */
    @GetMapping
    public ApiResponse<List<Cart>> list(HttpServletRequest servletRequest) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        return ApiResponse.success(cartService.listItems(userId));
    }

    /**
     * 添加商品到购物车；后续 Service 实现中要校验库存并处理已有商品数量累加。
     */
    @PostMapping
    public ApiResponse<Cart> add(HttpServletRequest servletRequest, @Valid @RequestBody CartAddRequest request) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        return ApiResponse.success(cartService.addItem(userId, request));
    }

    /**
     * 修改购物车商品数量；数量不能小于 1，也不能超过当前库存。
     */
    @PutMapping("/{id}")
    public ApiResponse<Cart> update(HttpServletRequest servletRequest,
                                    @PathVariable Integer id,
                                    @Valid @RequestBody CartUpdateRequest request) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        return ApiResponse.success(cartService.updateItem(userId, id, request));
    }

    /**
     * 删除当前用户自己的购物车项。
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest servletRequest, @PathVariable Integer id) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        cartService.deleteItem(userId, id);
        return ApiResponse.success(null);
    }

    /**
     * 批量删除购物车项，用于批量删除和清理失效商品。
     */
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(HttpServletRequest servletRequest,
                                         @Valid @RequestBody IdListRequest request) {
        Integer userId = (Integer) servletRequest.getAttribute("currentUserId");
        cartService.deleteItems(userId, request.getIds());
        return ApiResponse.success(null);
    }
}
