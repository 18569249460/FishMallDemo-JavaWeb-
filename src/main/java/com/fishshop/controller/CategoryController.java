package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.entity.Category;
import com.fishshop.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台商品分类接口。
 *
 * <p>分类浏览是公开接口，未登录用户也可以访问。</p>
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取全部分类，用于前台分类筛选栏。
     */
    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryService.listCategories());
    }
}
