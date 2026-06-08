package com.example.ticket.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticket.common.constant.OrderStatusConstants;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.payment.domain.PaymentRecordDO;
import com.example.ticket.payment.domain.PaymentReconcileIssueDO;
import com.example.ticket.payment.gateway.OrderStatusQueryGateway;
import com.example.ticket.payment.gateway.PaymentReconciledEventPublisher;
import com.example.ticket.payment.gateway.PaymentResultEventPublisher;
import com.example.ticket.payment.gateway.dto.OrderStatusDTO;
import com.example.ticket.payment.mapper.PaymentReconcileIssueMapper;
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
    private final PaymentReconcileIssueMapper paymentReconcileIssueMapper;
    private final PaymentResultEventPublisher paymentResultEventPublisher;
    private final PaymentReconciledEventPublisher paymentReconciledEventPublisher;
    private final OrderStatusQueryGateway orderStatusQueryGateway;
    private final int reconcileBatchSize;

    /**
     * 构造支付处理服务。
     *
     * @param paymentRecordMapper 支付记录 Mapper
     * @param paymentReconcileIssueMapper 支付对账异常 Mapper
     * @param paymentResultEventPublisher 支付结果事件发布器
     * @param paymentReconciledEventPublisher 支付收敛事件发布器
     * @param orderStatusQueryGateway 订单状态查询网关
     * @param reconcileBatchSize 单次对账批量大小
     */
    public PaymentProcessServiceImpl(
            PaymentRecordMapper paymentRecordMapper,
            PaymentReconcileIssueMapper paymentReconcileIssueMapper,
            PaymentResultEventPublisher paymentResultEventPublisher,
            PaymentReconciledEventPublisher paymentReconciledEventPublisher,
            OrderStatusQueryGateway orderStatusQueryGateway,
            @Value("${ticket.payment.reconcile-batch-size:100}") int reconcileBatchSize
    ) {
        this.paymentRecordMapper = paymentRecordMapper;
        this.paymentReconcileIssueMapper = paymentReconcileIssueMapper;
        this.paymentResultEventPublisher = paymentResultEventPublisher;
        this.paymentReconciledEventPublisher = paymentReconciledEventPublisher;
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
            // 先抢占对账处理权，避免多实例同时对同一条支付记录重复发布收敛事件。
            if (paymentRecordMapper.updateReconcileStatusIfCurrent(
                    record.getPaymentId(),
                    PaymentConstants.RECONCILE_STATUS_PENDING,
                    PaymentConstants.RECONCILE_STATUS_PROCESSING,
                    now
            ) <= 0) {
                continue;
            }
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
            // 对账收敛成功后，先清理历史异常，再在“支付成功且订单已到 PAID”时正式发布收敛事件。
            resolveIssueIfExists(record.getPaymentRequestId(), now);
            if (shouldPublishReconciledEvent(record, orderStatusDTO)) {
                paymentReconciledEventPublisher.publish(buildPaymentReconciledEvent(record));
            }
            updateReconcileState(
                    record.getPaymentId(),
                    PaymentConstants.RECONCILE_STATUS_PROCESSING,
                    PaymentConstants.RECONCILE_STATUS_DONE,
                    now
            );
            return;
        }

        // 支付事实与订单状态未收敛时，既要重发支付结果推动订单域继续收敛，也要留下正式异常事实供后续查询。
        paymentResultEventPublisher.publish(buildPaymentResultEvent(record));
        upsertReconcileIssue(record, orderStatusDTO, now);
        updateReconcileState(
                record.getPaymentId(),
                PaymentConstants.RECONCILE_STATUS_PROCESSING,
                PaymentConstants.RECONCILE_STATUS_PENDING,
                now
        );
    }

    /**
     * 更新支付记录对账状态。
     *
     * @param paymentId 支付标识
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @param now 当前时间
     */
    private void updateReconcileState(Long paymentId, String currentStatus, String targetStatus, LocalDateTime now) {
        paymentRecordMapper.updateReconcileStatusIfCurrent(paymentId, currentStatus, targetStatus, now);
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
     * 根据支付记录构造支付收敛事件。
     *
     * @param record 支付记录
     * @return 支付收敛事件
     */
    private PaymentReconciledEvent buildPaymentReconciledEvent(PaymentRecordDO record) {
        PaymentReconciledEvent event = new PaymentReconciledEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(PaymentEventConstants.PAYMENT_RECONCILED);
        event.setOccurredAt(Instant.now());
        event.setRequestId(record.getRequestId());
        event.setPaymentRequestId(record.getPaymentRequestId());
        event.setOrderId(record.getOrderId());
        event.setOrderNo(record.getOrderNo());
        event.setReservationId(record.getReservationId());
        event.setActivityId(record.getActivityId());
        event.setTicketId(record.getTicketId());
        event.setUserId(record.getUserId());
        event.setQuantity(record.getQuantity());
        event.setPaymentStatus(record.getPaymentStatus());
        event.setSource(PaymentConstants.PAYMENT_SOURCE_PAYMENT_SERVICE);
        return event;
    }

    /**
     * 判断当前对账收敛是否需要进一步发布支付收敛事件。
     *
     * @param record 支付记录
     * @param orderStatusDTO 订单状态结果
     * @return 是否需要发布收敛事件
     */
    private boolean shouldPublishReconciledEvent(PaymentRecordDO record, OrderStatusDTO orderStatusDTO) {
        return PaymentConstants.PAYMENT_STATUS_SUCCESS.equals(record.getPaymentStatus())
                && orderStatusDTO != null
                && OrderStatusConstants.PAID.equals(orderStatusDTO.getOrderStatus());
    }

    /**
     * 新增或更新支付对账异常事实。
     *
     * @param record 支付记录
     * @param orderStatusDTO 订单状态结果
     * @param now 当前时间
     */
    private void upsertReconcileIssue(PaymentRecordDO record, OrderStatusDTO orderStatusDTO, LocalDateTime now) {
        PaymentReconcileIssueDO existingIssue = paymentReconcileIssueMapper.selectOne(
                new LambdaQueryWrapper<PaymentReconcileIssueDO>()
                        .eq(PaymentReconcileIssueDO::getPaymentRequestId, record.getPaymentRequestId())
        );
        if (existingIssue == null) {
            PaymentReconcileIssueDO issue = new PaymentReconcileIssueDO();
            issue.setPaymentRequestId(record.getPaymentRequestId());
            issue.setOrderId(record.getOrderId());
            issue.setOrderNo(record.getOrderNo());
            issue.setReservationId(record.getReservationId());
            issue.setUserId(record.getUserId());
            issue.setActivityId(record.getActivityId());
            issue.setTicketId(record.getTicketId());
            issue.setQuantity(record.getQuantity());
            issue.setPaymentStatus(record.getPaymentStatus());
            issue.setOrderStatus(resolveOrderStatus(orderStatusDTO));
            issue.setIssueType(PaymentConstants.RECONCILE_ISSUE_TYPE_ORDER_STATUS_MISMATCH);
            issue.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_OPEN);
            issue.setLatestErrorMessage(buildIssueMessage(orderStatusDTO));
            issue.setFirstDetectedAt(now);
            issue.setLastDetectedAt(now);
            try {
                paymentReconcileIssueMapper.insert(issue);
                return;
            } catch (DuplicateKeyException exception) {
                // 并发回查时可能已有其他实例先写入异常事实，这里转入更新路径而不是让整批事务回滚。
                existingIssue = paymentReconcileIssueMapper.selectOne(
                        new LambdaQueryWrapper<PaymentReconcileIssueDO>()
                                .eq(PaymentReconcileIssueDO::getPaymentRequestId, record.getPaymentRequestId())
                );
                if (existingIssue == null) {
                    return;
                }
            }
        }

        PaymentReconcileIssueDO updateTarget = new PaymentReconcileIssueDO();
        updateTarget.setIssueId(existingIssue.getIssueId());
        updateTarget.setPaymentStatus(record.getPaymentStatus());
        updateTarget.setOrderStatus(resolveOrderStatus(orderStatusDTO));
        updateTarget.setIssueType(PaymentConstants.RECONCILE_ISSUE_TYPE_ORDER_STATUS_MISMATCH);
        updateTarget.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_OPEN);
        updateTarget.setLatestErrorMessage(buildIssueMessage(orderStatusDTO));
        updateTarget.setLastDetectedAt(now);
        updateTarget.setResolvedAt(null);
        paymentReconcileIssueMapper.updateById(updateTarget);
    }

    /**
     * 如果历史上存在未收敛异常，则在本次收敛成功后显式标记为已解决。
     *
     * @param paymentRequestId 支付请求标识
     * @param now 当前时间
     */
    private void resolveIssueIfExists(String paymentRequestId, LocalDateTime now) {
        PaymentReconcileIssueDO existingIssue = paymentReconcileIssueMapper.selectOne(
                new LambdaQueryWrapper<PaymentReconcileIssueDO>()
                        .eq(PaymentReconcileIssueDO::getPaymentRequestId, paymentRequestId)
        );
        if (existingIssue == null) {
            return;
        }

        PaymentReconcileIssueDO updateTarget = new PaymentReconcileIssueDO();
        updateTarget.setIssueId(existingIssue.getIssueId());
        updateTarget.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_RESOLVED);
        updateTarget.setResolvedAt(now);
        updateTarget.setLastDetectedAt(now);
        paymentReconcileIssueMapper.updateById(updateTarget);
    }

    /**
     * 解析当前订单状态。
     *
     * @param orderStatusDTO 订单状态查询结果
     * @return 订单状态
     */
    private String resolveOrderStatus(OrderStatusDTO orderStatusDTO) {
        return orderStatusDTO == null
                ? PaymentConstants.RECONCILE_ORDER_STATUS_NOT_FOUND
                : orderStatusDTO.getOrderStatus();
    }

    /**
     * 构造支付对账异常描述。
     *
     * @param orderStatusDTO 订单状态查询结果
     * @return 异常描述
     */
    private String buildIssueMessage(OrderStatusDTO orderStatusDTO) {
        if (orderStatusDTO == null) {
            return "对账未收敛，订单域未返回订单状态。";
        }
        return "对账未收敛，当前订单状态为 " + orderStatusDTO.getOrderStatus() + "。";
    }

    /**
     * 把支付记录状态映射为事件类型。
     *
     * @param paymentStatus 支付记录状态
     * @return 支付结果事件类型
     */
    private String mapPaymentStatusToEventType(String paymentStatus) {
        if (PaymentConstants.PAYMENT_STATUS_SUCCESS.equals(paymentStatus)) {
            return PaymentEventConstants.PAYMENT_SUCCEEDED;
        }
        if (PaymentConstants.PAYMENT_STATUS_FAILED.equals(paymentStatus)) {
            return PaymentEventConstants.PAYMENT_FAILED;
        }
        if (PaymentConstants.PAYMENT_STATUS_EXPIRED.equals(paymentStatus)) {
            return PaymentEventConstants.PAYMENT_EXPIRED;
        }
        throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID);
    }
}
