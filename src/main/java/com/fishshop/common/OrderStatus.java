package com.fishshop.common;

/**
 * 订单状态枚举，对应计划书中的“待发货 -> 待收货 -> 已完成”流转。
 */
public enum OrderStatus {
    WAIT_SHIP(0),
    WAIT_RECEIVE(1),
    COMPLETED(2),
    CANCELLED(3);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
