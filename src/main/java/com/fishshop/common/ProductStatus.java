package com.fishshop.common;

/**
 * 商品上下架状态。
 */
public enum ProductStatus {
    OFF_SHELF(0),
    ON_SHELF(1);

    private final int code;

    ProductStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
