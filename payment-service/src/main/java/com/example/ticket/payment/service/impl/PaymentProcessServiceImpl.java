package com.example.ticket.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.payment.domain.PaymentRecordDO;
import com.example.ticket.payment.gateway.OrderStatusQueryGateway;
import com.example.ticket.payment.gateway.PaymentResultEventPublisher;
import com.example.ticket.payment.gateway.dto.OrderStatusDTO;
import com.example.ticket.payment.mapper.PaymentRecordMapper;
import com.example.ticket.payment.request.PaymentNotifyRequest;
import com.example.ticket.payment.service.PaymentProcessService;
import com.example.ticket.payment.support.PaymentConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * 支付处理服务实现。
 * 用于在最小支付域内沉淀支付事实，并通过事件与对账回查驱动订单状态收敛。
 */
@Service
public class PaymentProcessServiceImpl implements PaymentProcessService {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final PaymentRecordMapper paymentRecordMapper;
    private final PaymentResultEventPublisher paymentResultEventPublisher;
    private final OrderStatusQueryGateway orderStatusQueryGateway;
    private final int reconcileBatchSize;

    /**
     * 构造支付处理服务。
     *
     * @param paymentRecordMapper 支付记录 Mapper
     * @param paymentResultEventPublisher 支付结果事件发布器
     * @param orderStatusQueryGateway 订单状态查询网关
     * @param reconcileBatchSize 单次对账批量大小
     */
    public PaymentProcessServiceImpl(
            PaymentRecordMapper paymentRecordMapper,
            PaymentResultEventPublisher paymentResultEventPublisher,
            OrderStatusQueryGateway orderStatusQueryGateway,
            @Value("${ticket.payment.reconcile-batch-size:100}") int reconcileBatchSize
    ) {
        this.paymentRecordMapper = paymentRecordMapper;
        this.paymentResultEventPublisher = paymentResultEventPublisher;
        this.orderStatusQueryGateway = orderStatusQueryGateway;
        this.reconcileBatchSize = reconcileBatchSize;
    }

    /**
     * 记录支付结果并发布事件。
     *
     * @param request 支付通知请求
     */
    @Override
    @Transactional
    public void recordPaymentResult(PaymentNotifyRequest request) {
        PaymentRecordDO existingRecord = paymentRecordMapper.selectOne(new LambdaQueryWrapper<PaymentRecordDO>()
                .eq(PaymentRecordDO::getPaymentRequestId, request.getPaymentRequestId()));
        if (existingRecord != null) {
            // 同一个支付请求只允许沉淀一条支付事实，重复通知直接忽略。
            return;
        }

        PaymentRecordDO record = buildPaymentRecord(request);
        try {
            paymentRecordMapper.insert(record);
        } catch (DuplicateKeyException exception) {
            // 并发重复通知时以数据库唯一键兜底，避免同一支付请求落两条事实。
            return;
        }
        paymentResultEventPublisher.publish(buildPaymentResultEvent(record));
    }

    /**
     * 对待收敛支付记录执行对账回查。
     *
     * @param now 当前时间
     */
    @Override
    @Transactional
    public void reconcilePendingPayments(LocalDateTime now) {
        List<PaymentRecordDO> records = paymentRecordMapper.selectList(new LambdaQueryWrapper<PaymentRecordDO>()
                .eq(PaymentRecordDO::getReconcileStatus, PaymentConstants.RECONCILE_STATUS_PENDING)
                .last("limit " + reconcileBatchSize));
        for (PaymentRecordDO record : records) {
            reconcileSingleRecord(record, now);
        }
    }

    /**
     * 对单条支付记录执行回查。
     *
     * @param record 支付记录
     * @param now 当前时间
     */
    private void reconcileSingleRecord(PaymentRecordDO record, LocalDateTime now) {
        OrderStatusDTO orderStatusDTO = orderStatusQueryGateway.queryOrderStatus(record.getOrderId());
        if (orderStatusDTO != null
                && PaymentConstants.isOrderConverged(record.getPaymentStatus(), orderStatusDTO.getOrderStatus())) {
            updateReconcileState(record.getPaymentId(), PaymentConstants.RECONCILE_STATUS_DONE, now);
            return;
        }

        // 支付事实与订单状态未收敛时，重发支付结果事件，交给订单域按幂等规则再次推进。
        paymentResultEventPublisher.publish(buildPaymentResultEvent(record));
        updateReconcileState(record.getPaymentId(), PaymentConstants.RECONCILE_STATUS_PENDING, now);
    }

    /**
     * 更新支付记录对账状态。
     *
     * @param paymentId 支付标识
     * @param reconcileStatus 对账状态
     * @param now 当前时间
     */
    private void updateReconcileState(Long paymentId, String reconcileStatus, LocalDateTime now) {
        PaymentRecordDO target = new PaymentRecordDO();
        target.setPaymentId(paymentId);
        target.setReconcileStatus(reconcileStatus);
        target.setLastReconcileAt(now);
        paymentRecordMapper.updateById(target);
    }

    /**
     * 构造支付记录对象。
     *
     * @param request 支付通知请求
     * @return 支付记录对象
     */
    private PaymentRecordDO buildPaymentRecord(PaymentNotifyRequest request) {
        String paymentStatus = PaymentConstants.mapEventTypeToPaymentStatus(request.getPaymentStatus());
        PaymentRecordDO record = new PaymentRecordDO();
        record.setPaymentRequestId(request.getPaymentRequestId());
        record.setOrderId(request.getOrderId());
        record.setOrderNo(request.getOrderNo());
        record.setReservationId(request.getReservationId());
        record.setRequestId(request.getRequestId());
        record.setUserId(request.getUserId());
        record.setActivityId(request.getActivityId());
        record.setTicketId(request.getTicketId());
        record.setQuantity(request.getQuantity());
        record.setPaymentStatus(paymentStatus);
        record.setReconcileStatus(PaymentConstants.RECONCILE_STATUS_PENDING);
        record.setReason(request.getReason());
        if (PaymentConstants.PAYMENT_STATUS_SUCCESS.equals(paymentStatus)) {
            record.setPaidAt(LocalDateTime.now(DEFAULT_ZONE_ID));
        }
        return record;
    }

    /**
     * 根据支付记录构造支付结果事件。
     *
     * @param record 支付记录
     * @return 支付结果事件
     */
    private PaymentResultEvent buildPaymentResultEvent(PaymentRecordDO record) {
        PaymentResultEvent event = new PaymentResultEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(mapPaymentStatusToEventType(record.getPaymentStatus()));
        event.setOccurredAt(Instant.now());
        event.setRequestId(record.getRequestId());
        event.setPaymentRequestId(record.getPaymentRequestId());
        event.setOrderId(record.getOrderId());
        event.setOrderNo(record.getOrderNo());
        event.setIdempotencyKey(record.getPaymentRequestId());
        event.setReservationId(record.getReservationId());
        event.setActivityId(record.getActivityId());
        event.setTicketId(record.getTicketId());
        event.setUserId(record.getUserId());
        event.setQuantity(record.getQuantity());
        event.setReason(record.getReason());
        event.setSource(PaymentConstants.PAYMENT_SOURCE_PAYMENT_SERVICE);
        return event;
    }

    /**
     * 把支付记录状态映射为事件类型。
     *
     * @param paymentStatus 支付记录状态
     * @return 支付结果事件类型
     */
    private String mapPaymentStatusToEventType(String paymentStatus) {
        if (PaymentConstants.PAYMENT_STATUS_SUCCESS.equals(paymentStatus)) {
            return com.example.ticket.common.event.payment.PaymentEventConstants.PAYMENT_SUCCEEDED;
        }
        if (PaymentConstants.PAYMENT_STATUS_FAILED.equals(paymentStatus)) {
            return com.example.ticket.common.event.payment.PaymentEventConstants.PAYMENT_FAILED;
        }
        if (PaymentConstants.PAYMENT_STATUS_EXPIRED.equals(paymentStatus)) {
            return com.example.ticket.common.event.payment.PaymentEventConstants.PAYMENT_EXPIRED;
        }
        throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID);
    }
}
