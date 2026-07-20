package com.fishshop.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理端首页统计数据。
 */
@Data
public class DashboardStatsDTO {
    private long userCount;
    private long productCount;
    private long orderCount;
    private long waitShipOrderCount;
    private long todayOrderCount;
    private long todayWaitShipOrderCount;
    private List<SalesTrendDTO> recentSales;
}
