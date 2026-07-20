package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.ProductQuery;
import com.fishshop.dto.ProductSaveRequest;
import com.fishshop.entity.Product;
import com.fishshop.service.ProductService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端商品维护接口。
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 后台商品列表，支持商品名称搜索。
     */
    @GetMapping
    public ApiResponse<List<Product>> list(@ModelAttribute ProductQuery query) {
        query.setIncludeInactive(Boolean.TRUE);
        return ApiResponse.success(productService.listProducts(query));
    }

    /**
     * 新增商品。
     */
    @PostMapping
    public ApiResponse<Product> create(@Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.success(productService.create(request));
    }

    /**
     * 编辑商品。
     */
    @PutMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Integer id,
                                       @Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.success(productService.update(id, request));
    }

    /**
     * 删除商品；历史订单通过订单商品快照保留原商品信息。
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        productService.delete(id);
        return ApiResponse.success(null);
    }
}
