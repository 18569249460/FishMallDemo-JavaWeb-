package com.fishshop.service.impl;

import com.fishshop.common.OrderStatus;
import com.fishshop.dto.DashboardStatsDTO;
import com.fishshop.dto.SalesTrendDTO;
import com.fishshop.mapper.OrderMapper;
import com.fishshop.mapper.ProductMapper;
import com.fishshop.mapper.UserMapper;
import com.fishshop.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘业务实现。
 *
 * <p>该模块只做简单聚合统计，不修改数据，因此不需要事务。</p>
 */
@Service
public class DashboardServiceImpl implements DashboardService {
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    public DashboardServiceImpl(UserMapper userMapper, ProductMapper productMapper, OrderMapper orderMapper) {
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
    }

    /**
     * 从用户、商品、订单 Mapper 中分别取统计值，组装为后台首页 DTO。
     */
    @Override
    public DashboardStatsDTO getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setUserCount(userMapper.countAll());
        stats.setProductCount(productMapper.countAll());
        stats.setOrderCount(orderMapper.countAll());
        stats.setWaitShipOrderCount(orderMapper.countByStatus(OrderStatus.WAIT_SHIP.getCode()));
        stats.setTodayOrderCount(orderMapper.countToday());
        stats.setTodayWaitShipOrderCount(orderMapper.countTodayByStatus(OrderStatus.WAIT_SHIP.getCode()));
        stats.setRecentSales(buildRecentSales());
        return stats;
    }

    /**
     * 补齐近七天中没有销量的日期，前端表格可以稳定显示 7 行。
     */
    private List<SalesTrendDTO> buildRecentSales() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        List<SalesTrendDTO> dbRows = orderMapper.selectRecentSales(startDate.toString());

        Map<String, Long> salesMap = new LinkedHashMap<String, Long>();
        for (SalesTrendDTO row : dbRows) {
            salesMap.put(row.getDate(), row.getSales());
        }

        List<SalesTrendDTO> recentSales = new ArrayList<SalesTrendDTO>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("M月d日");
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            SalesTrendDTO row = new SalesTrendDTO();
            row.setDate(date.format(labelFormatter));
            row.setSales(salesMap.containsKey(date.toString()) ? salesMap.get(date.toString()) : 0L);
            recentSales.add(row);
        }
        return recentSales;
    }
}
