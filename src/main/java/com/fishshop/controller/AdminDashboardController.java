package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.dto.DashboardStatsDTO;
import com.fishshop.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端仪表盘接口。
 *
 * <p>该接口位于 /api/admin/** 下，会由 AuthInterceptor 校验管理员权限。</p>
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取用户数、商品数、订单数和待发货订单数。
     */
    @GetMapping
    public ApiResponse<DashboardStatsDTO> stats() {
        return ApiResponse.success(dashboardService.getStats());
    }
}
