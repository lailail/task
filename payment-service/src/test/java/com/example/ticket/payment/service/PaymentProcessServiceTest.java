package com.example.ticket.payment.service;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.payment.domain.PaymentRecordDO;
import com.example.ticket.payment.gateway.PaymentResultEventPublisher;
import com.example.ticket.payment.gateway.OrderStatusQueryGateway;
import com.example.ticket.payment.gateway.dto.OrderStatusDTO;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    private PaymentProcessService paymentProcessService;

    /**
     * 构造被测支付处理服务。
     */
    @BeforeEach
    void setUp() {
        paymentProcessService = new PaymentProcessServiceImpl(
                paymentRecordMapper,
                paymentResultEventPublisher,
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
        when(orderStatusQueryGateway.queryOrderStatus(20001L)).thenReturn(buildOrderStatusDTO("CREATED"));

        paymentProcessService.reconcilePendingPayments(LocalDateTime.parse("2026-06-05T14:00:00"));

        verify(paymentResultEventPublisher).publish(any(PaymentResultEvent.class));
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
}
