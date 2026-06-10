package com.example.ticket.payment.controller;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.payment.service.PaymentTaskQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付域补偿任务内部查询控制器测试。
 * 用于验证控制层只负责转交请求并返回统一响应包装。
 */
class PaymentTaskInternalControllerTest {

    @Test
    void should_return_wrapped_page_response_when_query_payment_result_tasks() {
        PaymentTaskQueryService queryService = mock(PaymentTaskQueryService.class);
        PaymentTaskInternalController controller = new PaymentTaskInternalController(queryService);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();
        ReliableMessageTaskPageResponse pageResponse = new ReliableMessageTaskPageResponse();
        pageResponse.setRecords(List.of());
        when(queryService.queryPaymentResultTasks(request)).thenReturn(pageResponse);

        ApiResponse<ReliableMessageTaskPageResponse> response = controller.queryPaymentResultTasks(request);

        assertEquals(0, response.getCode());
        assertEquals(pageResponse, response.getData());
        verify(queryService).queryPaymentResultTasks(request);
    }
}
