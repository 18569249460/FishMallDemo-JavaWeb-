package com.fishshop.mapper;

import com.fishshop.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品表 Mapper。
 */
public interface ProductMapper {
    Product selectById(@Param("id") Integer id);

    List<Product> selectList(@Param("categoryId") Integer categoryId,
                             @Param("keyword") String keyword,
                             @Param("includeInactive") Boolean includeInactive);

    List<Product> selectHot(@Param("limit") int limit);

    int insert(Product product);

    int update(Product product);

    int deleteById(@Param("id") Integer id);

    int decreaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int increaseSales(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int increaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int decreaseSales(@Param("id") Integer id, @Param("quantity") Integer quantity);

    long countAll();
}
