package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.ProductQuery;
import com.fishshop.entity.Product;
import com.fishshop.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台商品浏览接口。
 *
 * <p>商品列表、热门商品和商品详情均允许未登录访问。</p>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品列表，支持 categoryId 分类筛选和 keyword 名称模糊搜索。
     */
    @GetMapping
    public ApiResponse<List<Product>> list(@ModelAttribute ProductQuery query) {
        return ApiResponse.success(productService.listProducts(query));
    }

    /**
     * 首页热门推荐，默认取销量最高的 8 个商品。
     */
    @GetMapping("/hot")
    public ApiResponse<List<Product>> hot() {
        return ApiResponse.success(productService.listHotProducts(8));
    }

    /**
     * 商品详情。
     */
    @GetMapping("/{id}")
    public ApiResponse<Product> detail(@PathVariable Integer id) {
        return ApiResponse.success(productService.getProduct(id));
    }
}
