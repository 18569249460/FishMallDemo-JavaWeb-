package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口，用于确认后端服务已经正常启动。
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    /**
     * 不依赖数据库，适合作为 Tomcat 部署后的第一步连通性检查。
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("fishMall backend is ready");
    }
}
