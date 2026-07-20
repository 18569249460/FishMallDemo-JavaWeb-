package com.fishshop.mapper;

import com.fishshop.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单商品快照表 Mapper。
 */
public interface OrderItemMapper {
    List<OrderItem> selectByOrderId(@Param("orderId") Integer orderId);

    int insertBatch(@Param("items") List<OrderItem> items);
}
