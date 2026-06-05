package com.example.ticket.order.service.impl;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.gateway.OrderStockReleaseEventPublisher;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.request.OrderCancelRequest;
import com.example.ticket.order.service.OrderCancelService;
import com.example.ticket.order.support.OrderConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 订单取消服务实现。
 * 用于处理用户主动取消待支付订单的业务校验、状态流转和库存释放事件发布。
 */
@Service
public class OrderCancelServiceImpl implements OrderCancelService {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final TicketOrderMapper ticketOrderMapper;
    private final OrderStockReleaseEventPublisher orderStockReleaseEventPublisher;

    /**
     * 构造订单取消服务。
     *
     * @param ticketOrderMapper 订单 Mapper
     * @param orderStockReleaseEventPublisher 库存释放事件发布器
     */
    public OrderCancelServiceImpl(
            TicketOrderMapper ticketOrderMapper,
            OrderStockReleaseEventPublisher orderStockReleaseEventPublisher
    ) {
        this.ticketOrderMapper = ticketOrderMapper;
        this.orderStockReleaseEventPublisher = orderStockReleaseEventPublisher;
    }

    /**
     * 取消指定订单。
     *
     * @param authenticatedUser 当前认证用户
     * @param orderId 订单标识
     * @param request 取消请求
     */
    @Override
    @Transactional
    public void cancelOrder(AuthenticatedUser authenticatedUser, Long orderId, OrderCancelRequest request) {
        TicketOrderDO order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(authenticatedUser.getUserId())) {
            throw new BusinessException(ErrorCode.ORDER_CANCEL_FORBIDDEN);
        }
        if (!OrderConstants.ORDER_STATUS_CREATED.equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        // 只有待支付订单允许用户主动取消，避免覆盖支付完成或系统关单终态。
        ticketOrderMapper.updateById(buildCancelledOrder(order));
        orderStockReleaseEventPublisher.publish(buildReleaseEvent(order, request));
    }

    /**
     * 构造已取消订单变更对象。
     *
     * @param order 原订单
     * @return 仅包含变更字段的订单对象
     */
    private TicketOrderDO buildCancelledOrder(TicketOrderDO order) {
        TicketOrderDO target = new TicketOrderDO();
        target.setOrderId(order.getOrderId());
        target.setOrderStatus(OrderConstants.ORDER_STATUS_CANCELLED);
        target.setClosedAt(LocalDateTime.now(DEFAULT_ZONE_ID));
        return target;
    }

    /**
     * 构造库存释放事件。
     *
     * @param order 原订单
     * @param request 取消请求
     * @return 库存释放事件
     */
    private StockReleaseEvent buildReleaseEvent(TicketOrderDO order, OrderCancelRequest request) {
        StockReleaseEvent releaseEvent = new StockReleaseEvent();
        releaseEvent.setEventId(UUID.randomUUID().toString());
        releaseEvent.setEventType(StockEventConstants.ORDER_CANCEL_RELEASE);
        releaseEvent.setOccurredAt(Instant.now());
        releaseEvent.setRequestId(request.getRequestId());
        releaseEvent.setIdempotencyKey(order.getIdempotencyKey());
        releaseEvent.setReservationId(order.getReservationId());
        releaseEvent.setOrderId(order.getOrderId());
        releaseEvent.setActivityId(order.getActivityId());
        releaseEvent.setTicketId(order.getTicketId());
        releaseEvent.setUserId(order.getUserId());
        releaseEvent.setQuantity(order.getQuantity());
        releaseEvent.setReason(request.getReason());
        releaseEvent.setSource(OrderConstants.ORDER_SOURCE_ORDER_SERVICE);
        return releaseEvent;
    }
}
