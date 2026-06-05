package com.example.ticket.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.order.domain.OrderEventLogDO;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.gateway.OrderCreateResultEventPublisher;
import com.example.ticket.order.mapper.OrderEventLogMapper;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.service.OrderCreateService;
import com.example.ticket.order.support.OrderConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 下单创建应用服务实现。
 * 用于把抢票预扣成功事件收敛为订单事实，并通过订单结果事件驱动后续预扣状态确认。
 */
@Service
public class OrderCreateServiceImpl implements OrderCreateService {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final TicketOrderMapper ticketOrderMapper;
    private final OrderEventLogMapper orderEventLogMapper;
    private final OrderCreateResultEventPublisher resultEventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 构造下单创建应用服务。
     *
     * @param ticketOrderMapper 订单表 Mapper
     * @param orderEventLogMapper 事件日志 Mapper
     * @param resultEventPublisher 结果事件发布器
     * @param objectMapper Jackson 对象映射器
     */
    public OrderCreateServiceImpl(
            TicketOrderMapper ticketOrderMapper,
            OrderEventLogMapper orderEventLogMapper,
            OrderCreateResultEventPublisher resultEventPublisher,
            ObjectMapper objectMapper
    ) {
        this.ticketOrderMapper = ticketOrderMapper;
        this.orderEventLogMapper = orderEventLogMapper;
        this.resultEventPublisher = resultEventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理下单请求事件。
     *
     * @param event 下单请求事件
     */
    @Override
    @Transactional
    public void handleOrderCreateRequested(OrderCreateRequestedEvent event) {
        if (queryEventLogByEventKey(event.getEventId()) != null) {
            return;
        }

        OrderCreateResultEvent resultEvent;
        TicketOrderDO existingOrder = queryOrderByIdempotencyKey(event.getIdempotencyKey());
        if (existingOrder != null) {
            insertSuccessEventLog(event);
            resultEvent = buildCreatedResultEvent(event, existingOrder);
            publishResultEventAfterCommit(resultEvent);
            return;
        }

        try {
            TicketOrderDO order = buildOrder(event);
            ticketOrderMapper.insert(order);
            insertSuccessEventLog(event);
            resultEvent = buildCreatedResultEvent(event, order);
        } catch (RuntimeException exception) {
            insertFailedEventLog(event, exception.getMessage());
            resultEvent = buildFailedResultEvent(event, exception.getMessage());
        }
        publishResultEventAfterCommit(resultEvent);
    }

    /**
     * 按事件键查询消费日志。
     *
     * @param eventKey 事件键
     * @return 消费日志
     */
    private OrderEventLogDO queryEventLogByEventKey(String eventKey) {
        return orderEventLogMapper.selectOne(new LambdaQueryWrapper<OrderEventLogDO>()
                .eq(OrderEventLogDO::getEventKey, eventKey));
    }

    /**
     * 按幂等键查询订单。
     *
     * @param idempotencyKey 幂等键
     * @return 订单对象
     */
    private TicketOrderDO queryOrderByIdempotencyKey(String idempotencyKey) {
        return ticketOrderMapper.selectOne(new LambdaQueryWrapper<TicketOrderDO>()
                .eq(TicketOrderDO::getIdempotencyKey, idempotencyKey));
    }

    /**
     * 构造订单对象。
     *
     * @param event 下单请求事件
     * @return 订单对象
     */
    private TicketOrderDO buildOrder(OrderCreateRequestedEvent event) {
        TicketOrderDO order = new TicketOrderDO();
        order.setOrderNo("ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setReservationId(event.getReservationId());
        order.setRequestId(event.getRequestId());
        order.setIdempotencyKey(event.getIdempotencyKey());
        order.setUserId(event.getUserId());
        order.setActivityId(event.getActivityId());
        order.setTicketId(event.getTicketId());
        order.setQuantity(event.getQuantity());
        // 当前阶段价格从票种表事实读取尚未接入，先保留最小金额占位，后续在订单定价链路补齐。
        order.setAmountCent(0);
        order.setOrderStatus(OrderConstants.ORDER_STATUS_CREATED);
        order.setSource(OrderEventConstants.SOURCE_ORDER_SERVICE);
        order.setExpireAt(toLocalDateTime(event.getExpireAt()));
        return order;
    }

    /**
     * 写入成功事件日志。
     *
     * @param event 下单请求事件
     */
    private void insertSuccessEventLog(OrderCreateRequestedEvent event) {
        OrderEventLogDO eventLog = new OrderEventLogDO();
        eventLog.setEventKey(event.getEventId());
        eventLog.setEventType(event.getEventType());
        eventLog.setBusinessKey(event.getReservationId());
        eventLog.setPayloadJson(buildPayloadJson(event));
        eventLog.setConsumeStatus(OrderConstants.EVENT_CONSUME_STATUS_SUCCESS);
        eventLog.setRetryCount(0);
        orderEventLogMapper.insert(eventLog);
    }

    /**
     * 写入失败事件日志。
     *
     * @param event 下单请求事件
     * @param errorMessage 错误信息
     */
    private void insertFailedEventLog(OrderCreateRequestedEvent event, String errorMessage) {
        OrderEventLogDO eventLog = new OrderEventLogDO();
        eventLog.setEventKey(event.getEventId());
        eventLog.setEventType(event.getEventType());
        eventLog.setBusinessKey(event.getReservationId());
        eventLog.setPayloadJson(buildPayloadJson(event));
        eventLog.setConsumeStatus(OrderConstants.EVENT_CONSUME_STATUS_FAILED);
        eventLog.setRetryCount(0);
        eventLog.setLastErrorMessage(errorMessage);
        orderEventLogMapper.insert(eventLog);
    }

    /**
     * 构造下单成功结果事件。
     *
     * @param event 下单请求事件
     * @param order 订单对象
     * @return 结果事件
     */
    private OrderCreateResultEvent buildCreatedResultEvent(OrderCreateRequestedEvent event, TicketOrderDO order) {
        OrderCreateResultEvent resultEvent = buildBaseResultEvent(event);
        resultEvent.setEventType(OrderEventConstants.ORDER_CREATED);
        resultEvent.setOrderId(order.getOrderId());
        resultEvent.setStatus(order.getOrderStatus());
        resultEvent.setSource(OrderEventConstants.SOURCE_ORDER_SERVICE);
        return resultEvent;
    }

    /**
     * 构造下单失败结果事件。
     *
     * @param event 下单请求事件
     * @param reason 失败原因
     * @return 结果事件
     */
    private OrderCreateResultEvent buildFailedResultEvent(OrderCreateRequestedEvent event, String reason) {
        OrderCreateResultEvent resultEvent = buildBaseResultEvent(event);
        resultEvent.setEventType(OrderEventConstants.ORDER_CREATE_FAILED);
        resultEvent.setStatus(OrderConstants.EVENT_CONSUME_STATUS_FAILED);
        resultEvent.setReason(reason);
        resultEvent.setSource(OrderEventConstants.SOURCE_ORDER_SERVICE);
        return resultEvent;
    }

    /**
     * 构造结果事件公共字段。
     *
     * @param event 下单请求事件
     * @return 结果事件
     */
    private OrderCreateResultEvent buildBaseResultEvent(OrderCreateRequestedEvent event) {
        OrderCreateResultEvent resultEvent = new OrderCreateResultEvent();
        resultEvent.setEventId(UUID.randomUUID().toString());
        resultEvent.setOccurredAt(Instant.now());
        resultEvent.setRequestId(event.getRequestId());
        resultEvent.setIdempotencyKey(event.getIdempotencyKey());
        resultEvent.setReservationId(event.getReservationId());
        resultEvent.setActivityId(event.getActivityId());
        resultEvent.setTicketId(event.getTicketId());
        resultEvent.setUserId(event.getUserId());
        resultEvent.setQuantity(event.getQuantity());
        return resultEvent;
    }

    /**
     * 在事务提交后发布结果事件。
     * 这样可以避免数据库事务尚未真正提交时就提前通知下游，导致预扣确认和订单事实不一致。
     *
     * @param resultEvent 下单结果事件
     */
    private void publishResultEventAfterCommit(OrderCreateResultEvent resultEvent) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            resultEventPublisher.publish(resultEvent);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 在本地事务成功提交后再把结果事件发给 MQ。
             */
            @Override
            public void afterCommit() {
                resultEventPublisher.publish(resultEvent);
            }
        });
    }

    /**
     * 构造最小事件载荷字符串。
     *
     * @param event 下单请求事件
     * @return 事件载荷
     */
    private String buildPayloadJson(OrderCreateRequestedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("serialize order create event failed", exception);
        }
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
