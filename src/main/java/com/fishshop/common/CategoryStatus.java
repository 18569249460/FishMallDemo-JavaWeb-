package com.fishshop.common;

/**
 * 分类启用状态。
 */
public enum CategoryStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    CategoryStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
