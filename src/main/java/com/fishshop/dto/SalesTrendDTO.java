package com.fishshop.dto;

import lombok.Data;

/**
 * 近七天销量趋势数据。
 */
@Data
public class SalesTrendDTO {
    private String date;
    private long sales;
}
