package com.example.ticket.payment.service;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.payment.domain.PaymentRecordDO;
import com.example.ticket.payment.domain.PaymentReconcileIssueDO;
import com.example.ticket.payment.mapper.PaymentReconcileIssueMapper;
import com.example.ticket.payment.mapper.PaymentRecordMapper;
import com.example.ticket.payment.request.PaymentReconcileIssueHandleRequest;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;
import com.example.ticket.payment.service.impl.PaymentReconcileIssueManageServiceImpl;
import com.example.ticket.payment.support.PaymentConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付对账异常人工治理服务单元测试。
 * 用于固定人工重试、忽略和解决时的状态推进与对账动作编排。
 */
@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class PaymentReconcileIssueManageServiceTest {

    @Mock
    private PaymentReconcileIssueMapper paymentReconcileIssueMapper;

    @Mock
    private PaymentRecordMapper paymentRecordMapper;

    @Mock
    private PaymentProcessService paymentProcessService;

    private PaymentReconcileIssueManageService paymentReconcileIssueManageService;

    /**
     * 构造被测人工治理服务。
     */
    @BeforeEach
    void setUp() {
        paymentReconcileIssueManageService = new PaymentReconcileIssueManageServiceImpl(
                paymentReconcileIssueMapper,
                paymentRecordMapper,
                paymentProcessService
        );
    }

    /**
     * 人工重试时，应重开异常、把支付记录置回待对账，并立即触发定向回查。
     */
    @Test
    void should_retry_issue_and_trigger_targeted_reconcile(CapturedOutput output) {
        PaymentReconcileIssueDO issue = buildOpenIssue();
        PaymentRecordDO paymentRecord = buildPaymentRecord();
        when(paymentReconcileIssueMapper.selectById(11L)).thenReturn(issue, issue);
        when(paymentRecordMapper.selectOne(any())).thenReturn(paymentRecord);

        PaymentReconcileIssueResponse response = paymentReconcileIssueManageService.retryIssue(11L, buildHandleRequest());

        ArgumentCaptor<PaymentReconcileIssueDO> issueCaptor = ArgumentCaptor.forClass(PaymentReconcileIssueDO.class);
        verify(paymentReconcileIssueMapper).updateById(issueCaptor.capture());
        assertEquals(PaymentConstants.RECONCILE_ISSUE_STATUS_OPEN, issueCaptor.getValue().getIssueStatus());
        assertEquals(PaymentConstants.RECONCILE_MANUAL_ACTION_RETRY, issueCaptor.getValue().getManualAction());

        ArgumentCaptor<PaymentRecordDO> recordCaptor = ArgumentCaptor.forClass(PaymentRecordDO.class);
        verify(paymentRecordMapper).updateById(recordCaptor.capture());
        assertEquals(PaymentConstants.RECONCILE_STATUS_PENDING, recordCaptor.getValue().getReconcileStatus());

        verify(paymentProcessService).reconcilePaymentRequest(any(), any());
        assertEquals(11L, response.getIssueId());
        assertTrue(output.getOut().contains("人工重试支付对账异常"));
        assertTrue(output.getOut().contains("issueId=11"));
        assertTrue(output.getOut().contains("paymentRequestId=payment-req-001"));
    }

    /**
     * 人工忽略时，应把异常标记为已忽略，并停止后续自动扫描。
     */
    @Test
    void should_ignore_issue_and_mark_payment_record_done() {
        PaymentReconcileIssueDO issue = buildOpenIssue();
        PaymentRecordDO paymentRecord = buildPaymentRecord();
        when(paymentReconcileIssueMapper.selectById(11L)).thenReturn(issue, issue);
        when(paymentRecordMapper.selectOne(any())).thenReturn(paymentRecord);

        PaymentReconcileIssueResponse response = paymentReconcileIssueManageService.ignoreIssue(11L, buildHandleRequest());

        ArgumentCaptor<PaymentReconcileIssueDO> issueCaptor = ArgumentCaptor.forClass(PaymentReconcileIssueDO.class);
        verify(paymentReconcileIssueMapper).updateById(issueCaptor.capture());
        assertEquals(PaymentConstants.RECONCILE_ISSUE_STATUS_IGNORED, issueCaptor.getValue().getIssueStatus());
        assertEquals(PaymentConstants.RECONCILE_MANUAL_ACTION_IGNORE, issueCaptor.getValue().getManualAction());

        ArgumentCaptor<PaymentRecordDO> recordCaptor = ArgumentCaptor.forClass(PaymentRecordDO.class);
        verify(paymentRecordMapper).updateById(recordCaptor.capture());
        assertEquals(PaymentConstants.RECONCILE_STATUS_DONE, recordCaptor.getValue().getReconcileStatus());
        assertEquals(11L, response.getIssueId());
    }

    /**
     * 人工解决时，应把异常标记为已解决，并停止后续自动扫描。
     */
    @Test
    void should_resolve_issue_and_mark_payment_record_done() {
        PaymentReconcileIssueDO issue = buildOpenIssue();
        PaymentRecordDO paymentRecord = buildPaymentRecord();
        when(paymentReconcileIssueMapper.selectById(11L)).thenReturn(issue, issue);
        when(paymentRecordMapper.selectOne(any())).thenReturn(paymentRecord);

        PaymentReconcileIssueResponse response = paymentReconcileIssueManageService.resolveIssue(11L, buildHandleRequest());

        ArgumentCaptor<PaymentReconcileIssueDO> issueCaptor = ArgumentCaptor.forClass(PaymentReconcileIssueDO.class);
        verify(paymentReconcileIssueMapper).updateById(issueCaptor.capture());
        assertEquals(PaymentConstants.RECONCILE_ISSUE_STATUS_RESOLVED, issueCaptor.getValue().getIssueStatus());
        assertEquals(PaymentConstants.RECONCILE_MANUAL_ACTION_RESOLVE, issueCaptor.getValue().getManualAction());

        ArgumentCaptor<PaymentRecordDO> recordCaptor = ArgumentCaptor.forClass(PaymentRecordDO.class);
        verify(paymentRecordMapper).updateById(recordCaptor.capture());
        assertEquals(PaymentConstants.RECONCILE_STATUS_DONE, recordCaptor.getValue().getReconcileStatus());
        assertEquals(11L, response.getIssueId());
    }

    /**
     * 非待治理状态的异常不允许再次执行人工忽略。
     */
    @Test
    void should_reject_ignore_when_issue_is_not_open() {
        PaymentReconcileIssueDO issue = buildOpenIssue();
        issue.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_RESOLVED);
        when(paymentReconcileIssueMapper.selectById(11L)).thenReturn(issue);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentReconcileIssueManageService.ignoreIssue(11L, buildHandleRequest()));

        assertEquals(ErrorCode.PAYMENT_RECONCILE_ISSUE_STATUS_INVALID.getCode(), exception.getCode());
    }

    /**
     * 构造人工处置请求。
     *
     * @return 人工处置请求
     */
    private PaymentReconcileIssueHandleRequest buildHandleRequest() {
        PaymentReconcileIssueHandleRequest request = new PaymentReconcileIssueHandleRequest();
        request.setOperator("tester");
        request.setNote("manual");
        return request;
    }

    /**
     * 构造待治理异常。
     *
     * @return 异常记录
     */
    private PaymentReconcileIssueDO buildOpenIssue() {
        PaymentReconcileIssueDO issue = new PaymentReconcileIssueDO();
        issue.setIssueId(11L);
        issue.setPaymentRequestId("payment-req-001");
        issue.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_OPEN);
        return issue;
    }

    /**
     * 构造支付记录。
     *
     * @return 支付记录
     */
    private PaymentRecordDO buildPaymentRecord() {
        PaymentRecordDO paymentRecord = new PaymentRecordDO();
        paymentRecord.setPaymentId(1L);
        paymentRecord.setPaymentRequestId("payment-req-001");
        paymentRecord.setReconcileStatus(PaymentConstants.RECONCILE_STATUS_PENDING);
        return paymentRecord;
    }
}
