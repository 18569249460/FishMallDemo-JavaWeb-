package com.fishshop.util;

/**
 * 手机号脱敏工具。
 *
 * <p>个人中心展示手机号时隐藏中间四位，降低敏感信息暴露风险。</p>
 */
public final class PhoneMaskUtil {
    private PhoneMaskUtil() {
    }

    /**
     * 将 13812345678 处理为 138****5678；异常长度时原样返回。
     */
    public static String mask(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
