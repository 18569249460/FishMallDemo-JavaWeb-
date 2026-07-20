package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 后台新增或编辑分类的请求参数。
 */
@Data
public class CategorySaveRequest {
    @NotBlank(message = "请输入分类名称")
    @Size(max = 50, message = "分类名称不能超过 50 个字符")
    private String name;

    @Min(value = 0, message = "分类排序不能小于 0")
    private Integer sortOrder;

    private Integer status;
}
