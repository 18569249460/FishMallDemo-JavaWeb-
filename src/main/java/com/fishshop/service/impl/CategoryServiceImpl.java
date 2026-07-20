package com.fishshop.service.impl;

import com.fishshop.common.BusinessException;
import com.fishshop.common.CategoryStatus;
import com.fishshop.common.ResultCode;
import com.fishshop.dto.CategorySaveRequest;
import com.fishshop.entity.Category;
import com.fishshop.mapper.CategoryMapper;
import com.fishshop.service.CategoryService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务实现。
 *
 * <p>分类同时服务前台筛选和后台维护，因此这里集中处理名称唯一性、存在性和删除保护。</p>
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * 查询全部分类，前台商品筛选栏和后台分类表共用。
     */
    @Override
    public List<Category> listCategories() {
        return categoryMapper.selectAll(Boolean.FALSE);
    }

    /**
     * 后台分类列表包含禁用分类和商品数量，方便管理端维护。
     */
    @Override
    public List<Category> listAdminCategories() {
        return categoryMapper.selectAll(Boolean.TRUE);
    }

    /**
     * 新增分类，分类名称由数据库唯一索引兜底防重复。
     */
    @Override
    public Category create(CategorySaveRequest request) {
        Category category = buildCategory(null, request);
        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "分类名称已存在");
        }
        return categoryMapper.selectById(category.getId());
    }

    /**
     * 修改分类名称，先确认分类存在，再交给数据库唯一索引保证名称不重复。
     */
    @Override
    public Category update(Integer id, CategorySaveRequest request) {
        ensureCategoryExists(id);
        Category category = buildCategory(id, request);
        try {
            categoryMapper.update(category);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "分类名称已存在");
        }
        return categoryMapper.selectById(id);
    }

    /**
     * 删除分类前必须确认该分类下没有商品，避免商品失去分类归属。
     */
    @Override
    public void delete(Integer id) {
        ensureCategoryExists(id);
        if (categoryMapper.countProductsById(id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该分类下存在商品，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    /**
     * 分类不存在时统一返回 404。
     */
    private void ensureCategoryExists(Integer id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "分类不存在");
        }
    }

    /**
     * 分类保存时统一处理排序和启用状态，避免新增/编辑逻辑不一致。
     */
    private Category buildCategory(Integer id, CategorySaveRequest request) {
        Category category = new Category();
        category.setId(id);
        category.setName(request.getName().trim());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(normalizeStatus(request.getStatus()));
        return category;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return CategoryStatus.ENABLED.getCode();
        }
        if (status != CategoryStatus.ENABLED.getCode() && status != CategoryStatus.DISABLED.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "分类状态无效");
        }
        return status;
    }
}
