package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.CategorySaveRequest;
import com.fishshop.entity.Category;
import com.fishshop.service.CategoryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端分类维护接口。
 */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 查询全部分类，包含禁用分类和商品数量。
     */
    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryService.listAdminCategories());
    }

    /**
     * 新增分类，分类名称需要唯一。
     */
    @PostMapping
    public ApiResponse<Category> create(@Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success(categoryService.create(request));
    }

    /**
     * 编辑分类名称。
     */
    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Integer id,
                                        @Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    /**
     * 删除分类；若分类下仍有商品，Service 应拒绝删除。
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ApiResponse.success(null);
    }
}
