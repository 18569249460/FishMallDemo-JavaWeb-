package com.fishshop.service;

import com.fishshop.dto.CategorySaveRequest;
import com.fishshop.entity.Category;

import java.util.List;

/**
 * 商品分类业务接口。
 */
public interface CategoryService {
    /**
     * 查询前台可见分类。
     */
    List<Category> listCategories();

    /**
     * 查询后台分类，包含禁用分类和商品数量。
     */
    List<Category> listAdminCategories();

    /**
     * 新增分类。
     */
    Category create(CategorySaveRequest request);

    /**
     * 修改分类名称。
     */
    Category update(Integer id, CategorySaveRequest request);

    /**
     * 删除分类，删除前需要校验分类下是否仍有商品。
     */
    void delete(Integer id);
}
