package com.example.ticket.order.service.impl;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.gateway.OrderStockReleaseEventPublisher;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.service.OrderPaymentService;
import com.example.ticket.order.support.OrderConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 订单支付结果处理服务实现。
 * 用于把支付结果事件转成显式订单状态流转，并在失败或过期时触发库存释放。
 */
@Service
public class OrderPaymentServiceImpl implements OrderPaymentService {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final TicketOrderMapper ticketOrderMapper;
    private final OrderStockReleaseEventPublisher orderStockReleaseEventPublisher;

    /**
     * 构造订单支付结果处理服务。
     *
     * @param ticketOrderMapper 订单 Mapper
     * @param orderStockReleaseEventPublisher 库存释放事件发布器
     */
    public OrderPaymentServiceImpl(
            TicketOrderMapper ticketOrderMapper,
            OrderStockReleaseEventPublisher orderStockReleaseEventPublisher
    ) {
        this.ticketOrderMapper = ticketOrderMapper;
        this.orderStockReleaseEventPublisher = orderStockReleaseEventPublisher;
    }

    /**
     * 处理支付结果事件。
     *
     * @param event 支付结果事件
     */
    @Override
    @Transactional
    public void handlePaymentResult(PaymentResultEvent event) {
        TicketOrderDO order = ticketOrderMapper.selectById(event.getOrderId());
        if (order == null || !OrderConstants.ORDER_STATUS_CREATED.equals(order.getOrderStatus())) {
            return;
        }

        if (PaymentEventConstants.PAYMENT_SUCCEEDED.equals(event.getEventType())) {
            // 只有待支付订单才能推进到已支付，避免重复支付结果覆盖终态。
            ticketOrderMapper.updateById(buildPaidOrder(order, event.getOccurredAt()));
            return;
        }

        if (PaymentEventConstants.PAYMENT_FAILED.equals(event.getEventType())
                || PaymentEventConstants.PAYMENT_EXPIRED.equals(event.getEventType())) {
            // 失败或过期都收敛到已取消，并统一通过库存释放事件驱动后续补偿。
            ticketOrderMapper.updateById(buildCancelledOrder(order));
            orderStockReleaseEventPublisher.publish(buildReleaseEvent(order, event));
        }
    }

    /**
     * 构造已支付订单变更对象。
     *
     * @param order 原订单
     * @param occurredAt 支付结果发生时间
     * @return 仅包含变更字段的订单对象
     */
    private TicketOrderDO buildPaidOrder(TicketOrderDO order, Instant occurredAt) {
        TicketOrderDO target = new TicketOrderDO();
        target.setOrderId(order.getOrderId());
        target.setOrderStatus(OrderConstants.ORDER_STATUS_PAID);
        target.setPaidAt(toLocalDateTime(occurredAt));
        return target;
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
     * @param event 支付结果事件
     * @return 库存释放事件
     */
    private StockReleaseEvent buildReleaseEvent(TicketOrderDO order, PaymentResultEvent event) {
        StockReleaseEvent releaseEvent = new StockReleaseEvent();
        releaseEvent.setEventId(UUID.randomUUID().toString());
        releaseEvent.setEventType(StockEventConstants.ORDER_CANCEL_RELEASE);
        releaseEvent.setOccurredAt(Instant.now());
        releaseEvent.setRequestId(event.getRequestId());
        releaseEvent.setIdempotencyKey(event.getIdempotencyKey());
        releaseEvent.setReservationId(order.getReservationId());
        releaseEvent.setOrderId(order.getOrderId());
        releaseEvent.setActivityId(order.getActivityId());
        releaseEvent.setTicketId(order.getTicketId());
        releaseEvent.setUserId(order.getUserId());
        releaseEvent.setQuantity(order.getQuantity());
        releaseEvent.setReason(event.getReason());
        releaseEvent.setSource(OrderConstants.ORDER_SOURCE_ORDER_SERVICE);
        return releaseEvent;
    }

    /**
     * 把绝对时间转换为本地时间。
     *
     * @param instant 绝对时间
     * @return 本地时间
     */
    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
    }
}
