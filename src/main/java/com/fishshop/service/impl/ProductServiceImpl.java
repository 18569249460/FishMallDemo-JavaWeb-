package com.fishshop.service.impl;

import com.fishshop.common.BusinessException;
import com.fishshop.common.ProductStatus;
import com.fishshop.common.ResultCode;
import com.fishshop.dto.ProductQuery;
import com.fishshop.dto.ProductSaveRequest;
import com.fishshop.entity.Product;
import com.fishshop.mapper.CategoryMapper;
import com.fishshop.mapper.ProductMapper;
import com.fishshop.service.ProductService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品业务实现。
 *
 * <p>商品浏览是公开接口；后台维护商品时需要校验分类存在、价格库存合法等基础规则。</p>
 */
@Service
public class ProductServiceImpl implements ProductService {
    private static final int DEFAULT_HOT_LIMIT = 8;
    private static final int MAX_HOT_LIMIT = 20;

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    public ProductServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
    }

    /**
     * 商品列表查询，支持分类筛选和名称模糊搜索；空查询对象时返回全部商品。
     */
    @Override
    public List<Product> listProducts(ProductQuery query) {
        Integer categoryId = query == null ? null : query.getCategoryId();
        String keyword = normalizeKeyword(query == null ? null : query.getKeyword());
        Boolean includeInactive = query == null ? Boolean.FALSE : query.getIncludeInactive();
        if (categoryId != null && categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "分类不存在");
        }
        return productMapper.selectList(categoryId, keyword, includeInactive);
    }

    /**
     * 热门商品用于首页推荐，限制最大条数避免前端误传过大 limit。
     */
    @Override
    public List<Product> listHotProducts(int limit) {
        int safeLimit = limit <= 0 ? DEFAULT_HOT_LIMIT : Math.min(limit, MAX_HOT_LIMIT);
        return productMapper.selectHot(safeLimit);
    }

    /**
     * 商品详情查询，不存在时返回 404。
     */
    @Override
    public Product getProduct(Integer id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != ProductStatus.ON_SHELF.getCode()) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品已下架");
        }
        return product;
    }

    /**
     * 后台新增商品，默认销量从 0 开始。
     */
    @Override
    public Product create(ProductSaveRequest request) {
        ensureCategoryExists(request.getCategoryId());
        Product product = buildProduct(null, request);
        product.setSales(0);
        productMapper.insert(product);
        return productMapper.selectById(product.getId());
    }

    /**
     * 后台编辑商品，保留历史销量，只更新基础展示和库存信息。
     */
    @Override
    public Product update(Integer id, ProductSaveRequest request) {
        ensureProductExists(id);
        ensureCategoryExists(request.getCategoryId());
        Product product = buildProduct(id, request);
        productMapper.update(product);
        return productMapper.selectById(id);
    }

    /**
     * 删除商品时先清理商品本身；购物车会因外键级联删除，历史订单快照仍保留。
     */
    @Transactional
    @Override
    public void delete(Integer id) {
        ensureProductExists(id);
        productMapper.deleteById(id);
    }

    /**
     * 把请求 DTO 转换为商品实体，字符串字段统一 trim，避免入库前后多余空格。
     */
    private Product buildProduct(Integer id, ProductSaveRequest request) {
        Product product = new Product();
        product.setId(id);
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSpecification(trimToNull(request.getSpecification()));
        product.setImageUrl(trimToNull(request.getImageUrl()));
        product.setDescription(trimToNull(request.getDescription()));
        product.setOrigin(trimToNull(request.getOrigin()));
        product.setShelfLife(trimToNull(request.getShelfLife()));
        product.setStorageMethod(trimToNull(request.getStorageMethod()));
        product.setDeliveryInfo(trimToNull(request.getDeliveryInfo()));
        product.setStatus(normalizeStatus(request.getStatus()));
        return product;
    }

    /**
     * 商品状态只允许上架或下架。
     */
    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return ProductStatus.ON_SHELF.getCode();
        }
        if (status != ProductStatus.ON_SHELF.getCode() && status != ProductStatus.OFF_SHELF.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品状态无效");
        }
        return status;
    }

    /**
     * 分类不存在时统一返回明确错误。
     */
    private void ensureCategoryExists(Integer categoryId) {
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "分类不存在");
        }
    }

    /**
     * 商品不存在时统一返回明确错误。
     */
    private void ensureProductExists(Integer productId) {
        if (productMapper.selectById(productId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
    }

    /**
     * 搜索关键字为空白时视为不筛选。
     */
    private String normalizeKeyword(String keyword) {
        return trimToNull(keyword);
    }

    /**
     * 去除字符串首尾空格，空白字符串转为 null。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
