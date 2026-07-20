package com.fishshop.util;

import java.util.Objects;

/**
 * 密码工具类。
 *
 * <p>当前项目按用户要求采用明文密码存储，工具类只负责统一保存和比较逻辑。</p>
 */
public final class PasswordUtil {
    private PasswordUtil() {
    }

    /**
     * 注册、后台新增账号时调用；当前直接返回明文密码写入数据库。
     */
    public static String encode(String rawPassword) {
        return rawPassword;
    }

    /**
     * 登录时调用，用用户输入的密码和数据库中保存的明文密码做精确匹配。
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        return Objects.equals(rawPassword, storedPassword);
    }
}
