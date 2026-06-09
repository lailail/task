package com.example.ticket.order.service.impl;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.common.event.order.OrderCompletedEvent;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.gateway.OrderCompletedEventPublisher;
import com.example.ticket.order.gateway.OrderStockReleaseEventPublisher;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.service.OrderPaymentService;
import com.example.ticket.order.support.OrderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(OrderPaymentServiceImpl.class);
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final TicketOrderMapper ticketOrderMapper;
    private final OrderStockReleaseEventPublisher orderStockReleaseEventPublisher;
    private final OrderCompletedEventPublisher orderCompletedEventPublisher;

    /**
     * 构造订单支付结果处理服务。
     *
     * @param ticketOrderMapper 订单 Mapper
     * @param orderStockReleaseEventPublisher 库存释放事件发布器
     * @param orderCompletedEventPublisher 订单完成事件发布器
     */
    public OrderPaymentServiceImpl(
            TicketOrderMapper ticketOrderMapper,
            OrderStockReleaseEventPublisher orderStockReleaseEventPublisher,
            OrderCompletedEventPublisher orderCompletedEventPublisher
    ) {
        this.ticketOrderMapper = ticketOrderMapper;
        this.orderStockReleaseEventPublisher = orderStockReleaseEventPublisher;
        this.orderCompletedEventPublisher = orderCompletedEventPublisher;
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
            log.warn("支付结果事件未触发订单状态流转，orderId={}, paymentRequestId={}, eventType={}, currentOrderStatus={}", event.getOrderId(), event.getPaymentRequestId(), event.getEventType(), order == null ? null : order.getOrderStatus());
            return;
        }

        if (PaymentEventConstants.PAYMENT_SUCCEEDED.equals(event.getEventType())) {
            // 只有真正完成 CREATED -> PAID 条件更新的线程，才算本次状态推进生效。
            int updated = ticketOrderMapper.markPaidIfCreated(order.getOrderId(), toLocalDateTime(event.getOccurredAt()));
            log.info("支付成功事件处理完成，orderId={}, paymentRequestId={}, updated={}, targetStatus={}", order.getOrderId(), event.getPaymentRequestId(), updated, OrderConstants.ORDER_STATUS_PAID);
            return;
        }

        if (PaymentEventConstants.PAYMENT_FAILED.equals(event.getEventType())
                || PaymentEventConstants.PAYMENT_EXPIRED.equals(event.getEventType())) {
            // 失败或过期都收敛到已取消，但只有条件更新成功后才允许继续发布释放事件。
            if (ticketOrderMapper.markCancelledIfCreated(order.getOrderId(), LocalDateTime.now(DEFAULT_ZONE_ID)) <= 0) {
                log.warn("支付失败事件未能推进订单取消，orderId={}, paymentRequestId={}, eventType={}", order.getOrderId(), event.getPaymentRequestId(), event.getEventType());
                return;
            }
            orderStockReleaseEventPublisher.publish(buildReleaseEvent(order, event));
            log.info("支付失败事件已取消订单并发布库存释放事件，orderId={}, paymentRequestId={}, eventType={}, reservationId={}", order.getOrderId(), event.getPaymentRequestId(), event.getEventType(), order.getReservationId());
        }
    }

    /**
     * 处理支付收敛事件。
     *
     * @param event 支付收敛事件
     */
    @Override
    @Transactional
    public void handlePaymentReconciled(PaymentReconciledEvent event) {
        TicketOrderDO order = ticketOrderMapper.selectById(event.getOrderId());
        if (order == null || !OrderConstants.ORDER_STATUS_PAID.equals(order.getOrderStatus())) {
            // 只有已支付订单才允许被推进到完成态，其他状态一律忽略，保证重复消费与非法状态都安全。
            log.warn("支付收敛事件未触发订单完成，orderId={}, paymentRequestId={}, currentOrderStatus={}", event.getOrderId(), event.getPaymentRequestId(), order == null ? null : order.getOrderStatus());
            return;
        }

        // 只有真正完成 PAID -> COMPLETED 条件更新的线程，才允许对外发布完成事件。
        if (ticketOrderMapper.markCompletedIfPaid(order.getOrderId()) <= 0) {
            log.warn("支付收敛事件命中并发竞争，未能推进订单完成，orderId={}, paymentRequestId={}", order.getOrderId(), event.getPaymentRequestId());
            return;
        }
        orderCompletedEventPublisher.publish(buildCompletedEvent(order, event));
        log.info("支付收敛事件已推进订单完成并发布完成事件，orderId={}, paymentRequestId={}, reservationId={}", order.getOrderId(), event.getPaymentRequestId(), order.getReservationId());
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
     * 构造订单完成事件。
     *
     * @param order 原订单
     * @param event 支付收敛事件
     * @return 订单完成事件
     */
    private OrderCompletedEvent buildCompletedEvent(TicketOrderDO order, PaymentReconciledEvent event) {
        OrderCompletedEvent completedEvent = new OrderCompletedEvent();
        completedEvent.setEventId(UUID.randomUUID().toString());
        completedEvent.setEventType(OrderConstants.ORDER_RESULT_TYPE_COMPLETED);
        completedEvent.setOccurredAt(Instant.now());
        completedEvent.setRequestId(order.getRequestId());
        completedEvent.setOrderId(order.getOrderId());
        completedEvent.setOrderNo(order.getOrderNo());
        completedEvent.setPaymentRequestId(event.getPaymentRequestId());
        completedEvent.setReservationId(order.getReservationId());
        completedEvent.setActivityId(order.getActivityId());
        completedEvent.setTicketId(order.getTicketId());
        completedEvent.setUserId(order.getUserId());
        completedEvent.setQuantity(order.getQuantity());
        completedEvent.setStatus(OrderConstants.ORDER_STATUS_COMPLETED);
        completedEvent.setSource(OrderConstants.ORDER_SOURCE_ORDER_SERVICE);
        return completedEvent;
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
