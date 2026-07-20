package com.fishshop.service.impl;

import com.fishshop.common.BusinessException;
import com.fishshop.common.OrderStatus;
import com.fishshop.common.ResultCode;
import com.fishshop.dto.OrderCreateRequest;
import com.fishshop.dto.ReceiverInfo;
import com.fishshop.entity.Cart;
import com.fishshop.entity.Order;
import com.fishshop.entity.OrderItem;
import com.fishshop.mapper.CartMapper;
import com.fishshop.mapper.OrderItemMapper;
import com.fishshop.mapper.OrderMapper;
import com.fishshop.mapper.ProductMapper;
import com.fishshop.service.OrderService;
import com.fishshop.util.OrderNoGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 订单业务实现。
 *
 * <p>创建订单时必须在同一事务中完成订单主表、订单商品快照、扣库存、加销量和清理购物车。</p>
 */
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartMapper cartMapper,
                            ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    /**
     * 提交订单。
     *
     * <p>金额以后端查询到的商品当前价格为准；扣库存使用 stock >= quantity 条件更新防止超卖。</p>
     */
    @Transactional
    @Override
    public Order createOrder(Integer userId, OrderCreateRequest request) {
        List<Integer> cartItemIds = distinctIds(request.getCartItemIds());
        if (cartItemIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请选择要结算的商品");
        }

        List<Cart> cartItems = cartMapper.selectByIds(userId, cartItemIds);
        if (cartItems.size() != cartItemIds.size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "购物车商品无效");
        }

        ReceiverInfo receiver = request.getReceiver();
        Order order = buildOrder(userId, receiver);
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : cartItems) {
            validateCartForOrder(cart);
            BigDecimal subtotal = cart.getProductPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setProductId(cart.getProductId());
            item.setProductName(cart.getProductName());
            item.setPrice(cart.getProductPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(subtotal);
            orderItems.add(item);
        }

        order.setTotalAmount(totalAmount);
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            int decreased = productMapper.decreaseStock(item.getProductId(), item.getQuantity());
            if (decreased == 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), item.getProductName() + " 库存不足");
            }
            productMapper.increaseSales(item.getProductId(), item.getQuantity());
        }

        orderItemMapper.insertBatch(orderItems);
        cartMapper.deleteByIds(userId, cartItemIds);

        order.setItems(orderItems);
        return order;
    }

    /**
     * 查询当前用户订单列表，并补充每个订单的商品快照。
     */
    @Override
    public List<Order> listMyOrders(Integer userId) {
        List<Order> orders = orderMapper.selectByUserId(userId);
        fillOrderItems(orders);
        return orders;
    }

    /**
     * 查询当前用户订单详情，必须校验订单归属。
     */
    @Override
    public Order getMyOrder(Integer userId, Integer orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        fillOrderItems(order);
        return order;
    }

    /**
     * 用户确认收货：只允许“待收货 -> 已完成”。
     */
    @Override
    public void confirmReceipt(Integer userId, Integer orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        int updated = orderMapper.updateStatus(orderId, OrderStatus.WAIT_RECEIVE.getCode(), OrderStatus.COMPLETED.getCode());
        if (updated == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前订单不能确认收货");
        }
    }

    /**
     * 用户取消待发货订单，并回滚库存和销量。
     */
    @Transactional
    @Override
    public void cancel(Integer userId, Integer orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.WAIT_SHIP.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前订单不能取消");
        }
        fillOrderItems(order);
        int updated = orderMapper.updateStatus(orderId, OrderStatus.WAIT_SHIP.getCode(), OrderStatus.CANCELLED.getCode());
        if (updated == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前订单不能取消");
        }
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() != null) {
                productMapper.increaseStock(item.getProductId(), item.getQuantity());
                productMapper.decreaseSales(item.getProductId(), item.getQuantity());
            }
        }
    }

    /**
     * 管理端查询订单列表，可按状态筛选。
     */
    @Override
    public List<Order> listAdminOrders(Integer status) {
        validateStatus(status);
        List<Order> orders = orderMapper.selectAll(status);
        fillOrderItems(orders);
        return orders;
    }

    /**
     * 管理端查询订单详情。
     */
    @Override
    public Order getAdminOrder(Integer orderId) {
        Order order = getOrderOrThrow(orderId);
        fillOrderItems(order);
        return order;
    }

    /**
     * 管理员发货：只允许“待发货 -> 待收货”。
     */
    @Override
    public void ship(Integer orderId, String trackingNo) {
        getOrderOrThrow(orderId);
        int updated = orderMapper.updateShip(orderId, trimToNull(trackingNo));
        if (updated == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前订单不能发货");
        }
    }

    /**
     * 创建订单主表对象，收货信息只做 trim，字段完整性由 DTO 参数校验保证。
     */
    private Order buildOrder(Integer userId, ReceiverInfo receiver) {
        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.next());
        order.setUserId(userId);
        order.setReceiverName(receiver.getReceiverName().trim());
        order.setReceiverPhone(receiver.getReceiverPhone().trim());
        order.setAddressDetail(receiver.getAddressDetail().trim());
        order.setStatus(OrderStatus.WAIT_SHIP.getCode());
        return order;
    }

    /**
     * 下单前再次校验购物车项中的商品价格、库存和数量。
     */
    private void validateCartForOrder(Cart cart) {
        if (cart.getQuantity() == null || cart.getQuantity() < 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "购物车商品数量无效");
        }
        if (cart.getProductPrice() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品价格无效");
        }
        if (cart.getProductStock() == null || cart.getProductStock() < cart.getQuantity()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), cart.getProductName() + " 库存不足");
        }
    }

    /**
     * 去重购物车项 ID，避免前端重复传同一个 ID 导致重复下单。
     */
    private List<Integer> distinctIds(List<Integer> ids) {
        List<Integer> result = new ArrayList<Integer>();
        if (ids == null) {
            return result;
        }
        Set<Integer> seen = new HashSet<Integer>();
        for (Integer id : ids) {
            if (id != null && seen.add(id)) {
                result.add(id);
            }
        }
        return result;
    }

    /**
     * 查询订单，不存在时统一返回 404。
     */
    private Order getOrderOrThrow(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }

    /**
     * 给订单列表批量补充商品明细。
     */
    private void fillOrderItems(List<Order> orders) {
        for (Order order : orders) {
            fillOrderItems(order);
        }
    }

    /**
     * 给单个订单补充商品明细。
     */
    private void fillOrderItems(Order order) {
        order.setItems(orderItemMapper.selectByOrderId(order.getId()));
    }

    /**
     * 管理端状态筛选只允许计划书定义的三种状态。
     */
    private void validateStatus(Integer status) {
        if (status == null) {
            return;
        }
        if (status != OrderStatus.WAIT_SHIP.getCode()
                && status != OrderStatus.WAIT_RECEIVE.getCode()
                && status != OrderStatus.COMPLETED.getCode()
                && status != OrderStatus.CANCELLED.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "订单状态无效");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
