package com.fishshop.common;

/**
 * 用户状态枚举：禁用用户不能登录或访问受保护接口。
 */
public enum UserStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
