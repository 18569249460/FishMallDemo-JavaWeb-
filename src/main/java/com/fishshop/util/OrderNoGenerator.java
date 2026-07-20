package com.fishshop.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单号生成工具。
 *
 * <p>格式为 FM + 年月日时分秒毫秒 + 4 位随机数，便于人工识别和后台查询。</p>
 */
public final class OrderNoGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private OrderNoGenerator() {
    }

    /**
     * 生成一个业务订单号；数据库唯一索引仍是最终防重复保障。
     */
    public static String next() {
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "FM" + FORMATTER.format(LocalDateTime.now()) + random;
    }
}
