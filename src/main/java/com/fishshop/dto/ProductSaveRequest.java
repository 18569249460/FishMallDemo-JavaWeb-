package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 后台新增或编辑商品的请求参数。
 */
@Data
public class ProductSaveRequest {
    @NotNull(message = "请选择商品分类")
    private Integer categoryId;

    @NotBlank(message = "请输入商品名称")
    @Size(max = 200, message = "商品名称不能超过 200 个字符")
    private String name;

    @NotNull(message = "请输入商品价格")
    @DecimalMin(value = "0.01", message = "商品价格必须大于 0")
    private BigDecimal price;

    @NotNull(message = "请输入商品库存")
    @Min(value = 0, message = "商品库存不能小于 0")
    private Integer stock;

    @Size(max = 100, message = "商品规格不能超过 100 个字符")
    private String specification;

    @Size(max = 200000, message = "图片内容过大，请更换较小图片")
    private String imageUrl;

    @Size(max = 100, message = "产地不能超过 100 个字符")
    private String origin;

    @Size(max = 100, message = "保质期不能超过 100 个字符")
    private String shelfLife;

    @Size(max = 100, message = "储存方式不能超过 100 个字符")
    private String storageMethod;

    @Size(max = 200, message = "配送说明不能超过 200 个字符")
    private String deliveryInfo;

    @NotNull(message = "请选择商品状态")
    private Integer status;

    private String description;
}
