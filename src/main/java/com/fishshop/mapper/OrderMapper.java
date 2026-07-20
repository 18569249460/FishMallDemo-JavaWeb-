package com.fishshop.mapper;

import com.fishshop.entity.Order;
import com.fishshop.dto.SalesTrendDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单主表 Mapper。
 */
public interface OrderMapper {
    Order selectById(@Param("id") Integer id);

    Order selectByOrderNo(@Param("orderNo") String orderNo);

    List<Order> selectByUserId(@Param("userId") Integer userId);

    List<Order> selectAll(@Param("status") Integer status);

    int insert(Order order);

    int updateStatus(@Param("id") Integer id, @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus);

    int updateShip(@Param("id") Integer id, @Param("trackingNo") String trackingNo);

    long countAll();

    long countByStatus(@Param("status") Integer status);

    long countToday();

    long countTodayByStatus(@Param("status") Integer status);

    List<SalesTrendDTO> selectRecentSales(@Param("startDate") String startDate);
}
