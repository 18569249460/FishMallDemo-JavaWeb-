package com.fishshop.service;

import com.fishshop.dto.DashboardStatsDTO;

/**
 * 管理端仪表盘业务接口。
 */
public interface DashboardService {
    /**
     * 汇总后台首页四个核心统计数字。
     */
    DashboardStatsDTO getStats();
}
