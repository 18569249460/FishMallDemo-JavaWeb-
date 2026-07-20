package com.fishshop.mapper;

import com.fishshop.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类表 Mapper。
 */
public interface CategoryMapper {
    Category selectById(@Param("id") Integer id);

    Category selectByName(@Param("name") String name);

    List<Category> selectAll(@Param("includeDisabled") Boolean includeDisabled);

    int insert(Category category);

    int update(Category category);

    int deleteById(@Param("id") Integer id);

    long countProductsById(@Param("id") Integer id);
}
