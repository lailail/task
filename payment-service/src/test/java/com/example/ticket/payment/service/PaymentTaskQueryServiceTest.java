package com.example.ticket.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.reliable.query.ReliableMessageTaskTypes;
import com.example.ticket.payment.domain.PaymentReconciledTaskDO;
import com.example.ticket.payment.domain.PaymentResultTaskDO;
import com.example.ticket.payment.mapper.PaymentReconciledTaskMapper;
import com.example.ticket.payment.mapper.PaymentResultTaskMapper;
import com.example.ticket.payment.service.impl.PaymentTaskQueryServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 支付域补偿任务查询服务测试。
 * 用于验证两类支付补偿任务都能映射到统一后台查询模型。
 */
class PaymentTaskQueryServiceTest {

    @Test
    void should_map_payment_result_tasks() {
        PaymentResultTaskMapper resultTaskMapper = mock(PaymentResultTaskMapper.class);
        PaymentReconciledTaskMapper reconciledTaskMapper = mock(PaymentReconciledTaskMapper.class);
        PaymentTaskQueryServiceImpl service = new PaymentTaskQueryServiceImpl(resultTaskMapper, reconciledTaskMapper);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();

        PaymentResultTaskDO record = new PaymentResultTaskDO();
        record.setTaskId(41L);
        record.setEventKey("evt-payment-result-1");
        record.setEventType("ticket.payment.result");
        record.setBusinessKey("payment-1");
        record.setTaskStatus("SENT");

        Page<PaymentResultTaskDO> mapperPage = new Page<>(1L, 10L);
        mapperPage.setRecords(List.of(record));
        mapperPage.setTotal(1L);
        when(resultTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        ReliableMessageTaskPageResponse response = service.queryPaymentResultTasks(request);

        assertEquals(1L, response.getTotal());
        assertEquals(ReliableMessageTaskTypes.PAYMENT_RESULT, response.getRecords().get(0).getTaskType());
    }

    @Test
    void should_map_payment_reconciled_tasks() {
        PaymentResultTaskMapper resultTaskMapper = mock(PaymentResultTaskMapper.class);
        PaymentReconciledTaskMapper reconciledTaskMapper = mock(PaymentReconciledTaskMapper.class);
        PaymentTaskQueryServiceImpl service = new PaymentTaskQueryServiceImpl(resultTaskMapper, reconciledTaskMapper);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();

        PaymentReconciledTaskDO record = new PaymentReconciledTaskDO();
        record.setTaskId(51L);
        record.setEventKey("evt-payment-reconciled-1");
        record.setEventType("ticket.payment.reconciled");
        record.setBusinessKey("payment-2");
        record.setTaskStatus("RETRYING");

        Page<PaymentReconciledTaskDO> mapperPage = new Page<>(1L, 10L);
        mapperPage.setRecords(List.of(record));
        mapperPage.setTotal(1L);
        when(reconciledTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        ReliableMessageTaskPageResponse response = service.queryPaymentReconciledTasks(request);

        assertEquals(1L, response.getTotal());
        assertEquals(ReliableMessageTaskTypes.PAYMENT_RECONCILED, response.getRecords().get(0).getTaskType());
    }
}
