package com.fishshop.service;

import com.fishshop.dto.ProductQuery;
import com.fishshop.dto.ProductSaveRequest;
import com.fishshop.entity.Product;

import java.util.List;

/**
 * 商品业务接口。
 */
public interface ProductService {
    /**
     * 查询商品列表，支持分类和名称筛选。
     */
    List<Product> listProducts(ProductQuery query);

    /**
     * 查询热门商品，用于首页推荐。
     */
    List<Product> listHotProducts(int limit);

    /**
     * 查询商品详情。
     */
    Product getProduct(Integer id);

    /**
     * 后台新增商品。
     */
    Product create(ProductSaveRequest request);

    /**
     * 后台编辑商品。
     */
    Product update(Integer id, ProductSaveRequest request);

    /**
     * 后台删除商品。
     */
    void delete(Integer id);
}
