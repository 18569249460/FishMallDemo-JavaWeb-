package com.fishshop.service.impl;

import com.fishshop.common.BusinessException;
import com.fishshop.common.ProductStatus;
import com.fishshop.common.ResultCode;
import com.fishshop.dto.CartAddRequest;
import com.fishshop.dto.CartUpdateRequest;
import com.fishshop.entity.Cart;
import com.fishshop.entity.Product;
import com.fishshop.mapper.CartMapper;
import com.fishshop.mapper.ProductMapper;
import com.fishshop.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车业务实现。
 *
 * <p>购物车所有操作都必须基于当前登录用户 ID，避免用户通过传入购物车项 ID 操作他人数据。</p>
 */
@Service
public class CartServiceImpl implements CartService {
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CartServiceImpl(CartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    /**
     * 查询当前用户购物车列表，返回时联表带出商品价格、库存和图片。
     */
    @Override
    public List<Cart> listItems(Integer userId) {
        return cartMapper.selectByUserId(userId);
    }

    /**
     * 添加商品到购物车；如果购物车已有该商品，则累加数量，但总数量不能超过当前库存。
     */
    @Override
    public Cart addItem(Integer userId, CartAddRequest request) {
        Product product = getAvailableProduct(request.getProductId());
        int targetQuantity = request.getQuantity();
        Cart existing = cartMapper.selectByUserIdAndProductId(userId, request.getProductId());
        if (existing != null) {
            targetQuantity += existing.getQuantity();
        }
        ensureQuantityWithinStock(targetQuantity, product.getStock());

        if (existing != null) {
            cartMapper.updateQuantity(existing.getId(), userId, targetQuantity);
            return cartMapper.selectById(existing.getId());
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(request.getProductId());
        cart.setQuantity(targetQuantity);
        cartMapper.insert(cart);
        return cartMapper.selectById(cart.getId());
    }

    /**
     * 修改购物车商品数量；先做归属校验，再做库存校验。
     */
    @Override
    public Cart updateItem(Integer userId, Integer cartItemId, CartUpdateRequest request) {
        Cart cart = getOwnedCartItem(userId, cartItemId);
        Product product = getAvailableProduct(cart.getProductId());
        ensureQuantityWithinStock(request.getQuantity(), product.getStock());
        int updated = cartMapper.updateQuantity(cartItemId, userId, request.getQuantity());
        if (updated == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车项不存在");
        }
        return cartMapper.selectById(cartItemId);
    }

    /**
     * 删除购物车项；WHERE 中同时带 id 和 user_id，防止越权删除。
     */
    @Override
    public void deleteItem(Integer userId, Integer cartItemId) {
        int deleted = cartMapper.deleteById(cartItemId, userId);
        if (deleted == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车项不存在");
        }
    }

    /**
     * 批量删除购物车项，用于购物车页的批量删除和失效商品清理。
     */
    @Override
    public void deleteItems(Integer userId, List<Integer> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请选择要删除的商品");
        }
        int deleted = cartMapper.deleteByIds(userId, cartItemIds);
        if (deleted == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车项不存在");
        }
    }

    /**
     * 查询商品并校验商品存在且仍有库存。
     */
    private Product getAvailableProduct(Integer productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != ProductStatus.ON_SHELF.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品已下架");
        }
        if (product.getStock() == null || product.getStock() <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品已售罄");
        }
        return product;
    }

    /**
     * 查询购物车项并确认属于当前用户。
     */
    private Cart getOwnedCartItem(Integer userId, Integer cartItemId) {
        Cart cart = cartMapper.selectById(cartItemId);
        if (cart == null || !userId.equals(cart.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车项不存在");
        }
        return cart;
    }

    /**
     * 购物车数量不能超过商品实时库存。
     */
    private void ensureQuantityWithinStock(Integer quantity, Integer stock) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "购买数量必须大于 0");
        }
        if (stock == null || quantity > stock) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "购买数量超过库存");
        }
    }
}
