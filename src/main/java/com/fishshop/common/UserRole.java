package com.fishshop.common;

/**
 * 用户角色枚举：0 为普通用户，1 为管理员。
 */
public enum UserRole {
    USER(0),
    ADMIN(1);

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
