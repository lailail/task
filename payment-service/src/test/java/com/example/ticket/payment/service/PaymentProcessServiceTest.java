package com.example.ticket.payment.service;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.payment.domain.PaymentRecordDO;
import com.example.ticket.payment.domain.PaymentReconcileIssueDO;
import com.example.ticket.payment.gateway.PaymentResultEventPublisher;
import com.example.ticket.payment.gateway.OrderStatusQueryGateway;
import com.example.ticket.payment.gateway.PaymentReconciledEventPublisher;
import com.example.ticket.payment.gateway.dto.OrderStatusDTO;
import com.example.ticket.payment.mapper.PaymentReconcileIssueMapper;
import com.example.ticket.payment.mapper.PaymentRecordMapper;
import com.example.ticket.payment.request.PaymentNotifyRequest;
import com.example.ticket.payment.service.impl.PaymentProcessServiceImpl;
import com.example.ticket.payment.support.PaymentConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付处理服务单元测试。
 * 用于固定“模拟支付结果落库、发布支付结果事件、对账不一致时重发”的核心边界。
 */
@ExtendWith(MockitoExtension.class)
class PaymentProcessServiceTest {

    @Mock
    private PaymentRecordMapper paymentRecordMapper;

    @Mock
    private PaymentResultEventPublisher paymentResultEventPublisher;

    @Mock
    private OrderStatusQueryGateway orderStatusQueryGateway;

    @Mock
    private PaymentReconcileIssueMapper paymentReconcileIssueMapper;

    @Mock
    private PaymentReconciledEventPublisher paymentReconciledEventPublisher;

    private PaymentProcessService paymentProcessService;

    /**
     * 构造被测支付处理服务。
     */
    @BeforeEach
    void setUp() {
        paymentProcessService = new PaymentProcessServiceImpl(
                paymentRecordMapper,
                paymentReconcileIssueMapper,
                paymentResultEventPublisher,
                paymentReconciledEventPublisher,
                orderStatusQueryGateway,
                50
        );
    }

    /**
     * 模拟支付成功时，应落库支付事实并发布支付成功事件。
     */
    @Test
    void should_record_payment_and_publish_result_when_notify_request_arrives() {
        PaymentNotifyRequest request = buildNotifyRequest(PaymentEventConstants.PAYMENT_SUCCEEDED);
        when(paymentRecordMapper.selectOne(any())).thenReturn(null);
        when(paymentRecordMapper.insert(any(PaymentRecordDO.class))).thenReturn(1);

        paymentProcessService.recordPaymentResult(request);

        verify(paymentRecordMapper).insert(any(PaymentRecordDO.class));
        ArgumentCaptor<PaymentResultEvent> eventCaptor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(paymentResultEventPublisher).publish(eventCaptor.capture());
        assertEquals(PaymentEventConstants.PAYMENT_SUCCEEDED, eventCaptor.getValue().getEventType());
        assertEquals(20001L, eventCaptor.getValue().getOrderId());
    }

    /**
     * 对账发现支付成功但订单状态尚未收敛时，应重发支付结果事件。
     */
    @Test
    void should_republish_payment_result_when_reconcile_detects_order_not_converged() {
        PaymentRecordDO record = buildSuccessPaymentRecord();
        when(paymentRecordMapper.selectList(any())).thenReturn(List.of(record));
        when(paymentRecordMapper.updateReconcileStatusIfCurrent(
                1L,
                PaymentConstants.RECONCILE_STATUS_PENDING,
                PaymentConstants.RECONCILE_STATUS_PROCESSING,
                LocalDateTime.parse("2026-06-05T14:00:00")
        )).thenReturn(1);
        when(paymentRecordMapper.updateReconcileStatusIfCurrent(
                1L,
                PaymentConstants.RECONCILE_STATUS_PROCESSING,
                PaymentConstants.RECONCILE_STATUS_PENDING,
                LocalDateTime.parse("2026-06-05T14:00:00")
        )).thenReturn(1);
        when(orderStatusQueryGateway.queryOrderStatus(20001L)).thenReturn(buildOrderStatusDTO("CREATED"));
        when(paymentReconcileIssueMapper.selectOne(any())).thenReturn(null);

        paymentProcessService.reconcilePendingPayments(LocalDateTime.parse("2026-06-05T14:00:00"));

        verify(paymentResultEventPublisher).publish(any(PaymentResultEvent.class));
        verify(paymentReconcileIssueMapper).insert(any(PaymentReconcileIssueDO.class));
    }

    /**
     * 对账发现支付成功且订单已经推进到 PAID 时，应发布支付收敛事件并解决历史异常。
     */
    @Test
    void should_publish_reconciled_event_and_resolve_issue_when_payment_converges_to_paid() {
        PaymentRecordDO record = buildSuccessPaymentRecord();
        PaymentReconcileIssueDO issue = buildOpenIssue();
        when(paymentRecordMapper.selectList(any())).thenReturn(List.of(record));
        when(paymentRecordMapper.updateReconcileStatusIfCurrent(
                1L,
                PaymentConstants.RECONCILE_STATUS_PENDING,
                PaymentConstants.RECONCILE_STATUS_PROCESSING,
                LocalDateTime.parse("2026-06-05T14:05:00")
        )).thenReturn(1);
        when(paymentRecordMapper.updateReconcileStatusIfCurrent(
                1L,
                PaymentConstants.RECONCILE_STATUS_PROCESSING,
                PaymentConstants.RECONCILE_STATUS_DONE,
                LocalDateTime.parse("2026-06-05T14:05:00")
        )).thenReturn(1);
        when(orderStatusQueryGateway.queryOrderStatus(20001L)).thenReturn(buildOrderStatusDTO("PAID"));
        when(paymentReconcileIssueMapper.selectOne(any())).thenReturn(issue);

        paymentProcessService.reconcilePendingPayments(LocalDateTime.parse("2026-06-05T14:05:00"));

        verify(paymentReconciledEventPublisher).publish(any(PaymentReconciledEvent.class));
        verify(paymentReconcileIssueMapper).updateById(any(PaymentReconcileIssueDO.class));
    }

    /**
     * 如果当前待对账记录已被其他工作线程抢占，则当前线程应直接跳过，不再重复发布事件。
     */
    @Test
    void should_skip_reconcile_record_when_another_worker_already_claimed_it() {
        PaymentRecordDO record = buildSuccessPaymentRecord();
        when(paymentRecordMapper.selectList(any())).thenReturn(List.of(record));
        when(paymentRecordMapper.updateReconcileStatusIfCurrent(
                1L,
                PaymentConstants.RECONCILE_STATUS_PENDING,
                PaymentConstants.RECONCILE_STATUS_PROCESSING,
                LocalDateTime.parse("2026-06-05T14:10:00")
        )).thenReturn(0);

        paymentProcessService.reconcilePendingPayments(LocalDateTime.parse("2026-06-05T14:10:00"));

        verify(orderStatusQueryGateway, never()).queryOrderStatus(any());
        verify(paymentResultEventPublisher, never()).publish(any(PaymentResultEvent.class));
        verify(paymentReconciledEventPublisher, never()).publish(any(PaymentReconciledEvent.class));
    }

    /**
     * 如果异常事实在并发下被其他工作线程先插入，则当前线程应兜底更新而不是整批回滚。
     */
    @Test
    void should_continue_reconcile_when_issue_insert_hits_duplicate_key() {
        PaymentRecordDO record = buildSuccessPaymentRecord();
        PaymentReconcileIssueDO existingIssue = buildOpenIssue();
        LocalDateTime now = LocalDateTime.parse("2026-06-05T14:15:00");
        when(paymentRecordMapper.selectList(any())).thenReturn(List.of(record));
        when(paymentRecordMapper.updateReconcileStatusIfCurrent(
                1L,
                PaymentConstants.RECONCILE_STATUS_PENDING,
                PaymentConstants.RECONCILE_STATUS_PROCESSING,
                now
        )).thenReturn(1);
        when(paymentRecordMapper.updateReconcileStatusIfCurrent(
                1L,
                PaymentConstants.RECONCILE_STATUS_PROCESSING,
                PaymentConstants.RECONCILE_STATUS_PENDING,
                now
        )).thenReturn(1);
        when(orderStatusQueryGateway.queryOrderStatus(20001L)).thenReturn(buildOrderStatusDTO("CREATED"));
        when(paymentReconcileIssueMapper.selectOne(any())).thenReturn(null, existingIssue);
        when(paymentReconcileIssueMapper.insert(any(PaymentReconcileIssueDO.class)))
                .thenThrow(new DuplicateKeyException("duplicate"));

        paymentProcessService.reconcilePendingPayments(now);

        verify(paymentReconcileIssueMapper).updateById(any(PaymentReconcileIssueDO.class));
    }

    /**
     * 构造支付通知请求。
     *
     * @param eventType 支付事件类型
     * @return 支付通知请求
     */
    private PaymentNotifyRequest buildNotifyRequest(String eventType) {
        PaymentNotifyRequest request = new PaymentNotifyRequest();
        request.setRequestId("pay-req-001");
        request.setPaymentRequestId("payment-req-001");
        request.setOrderId(20001L);
        request.setOrderNo("ORD-001");
        request.setReservationId("reservation-001");
        request.setUserId(10001L);
        request.setActivityId(1001L);
        request.setTicketId(501L);
        request.setQuantity(1);
        request.setPaymentStatus(eventType);
        request.setReason("MOCK_NOTIFY");
        return request;
    }

    /**
     * 构造已支付但未完成收敛的支付记录。
     *
     * @return 支付记录
     */
    private PaymentRecordDO buildSuccessPaymentRecord() {
        PaymentRecordDO record = new PaymentRecordDO();
        record.setPaymentId(1L);
        record.setPaymentRequestId("payment-req-001");
        record.setOrderId(20001L);
        record.setOrderNo("ORD-001");
        record.setReservationId("reservation-001");
        record.setRequestId("pay-req-001");
        record.setPaymentStatus(PaymentConstants.PAYMENT_STATUS_SUCCESS);
        record.setReconcileStatus(PaymentConstants.RECONCILE_STATUS_PENDING);
        record.setUserId(10001L);
        record.setActivityId(1001L);
        record.setTicketId(501L);
        record.setQuantity(1);
        return record;
    }

    /**
     * 构造订单状态查询结果。
     *
     * @param orderStatus 订单状态
     * @return 订单状态结果
     */
    private OrderStatusDTO buildOrderStatusDTO(String orderStatus) {
        OrderStatusDTO dto = new OrderStatusDTO();
        dto.setOrderId(20001L);
        dto.setOrderStatus(orderStatus);
        return dto;
    }

    /**
     * 构造已打开的对账异常记录。
     *
     * @return 对账异常记录
     */
    private PaymentReconcileIssueDO buildOpenIssue() {
        PaymentReconcileIssueDO issue = new PaymentReconcileIssueDO();
        issue.setIssueId(11L);
        issue.setPaymentRequestId("payment-req-001");
        issue.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_OPEN);
        return issue;
    }
}
