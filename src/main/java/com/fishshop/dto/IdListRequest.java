package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 通用 ID 列表请求，用于批量删除等操作。
 */
@Data
public class IdListRequest {
    @NotEmpty(message = "请选择要操作的数据")
    private List<Integer> ids;
}
