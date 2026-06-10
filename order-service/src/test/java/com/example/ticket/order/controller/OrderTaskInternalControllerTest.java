package com.example.ticket.order.controller;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.order.service.OrderTaskQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单域补偿任务内部查询控制器测试。
 * 用于验证控制层只负责转交请求并包装响应。
 */
class OrderTaskInternalControllerTest {

    @Test
    void should_return_wrapped_page_response_when_query_order_result_tasks() {
        OrderTaskQueryService queryService = mock(OrderTaskQueryService.class);
        OrderTaskInternalController controller = new OrderTaskInternalController(queryService);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();
        ReliableMessageTaskPageResponse pageResponse = new ReliableMessageTaskPageResponse();
        pageResponse.setRecords(List.of());
        when(queryService.queryOrderResultTasks(request)).thenReturn(pageResponse);

        ApiResponse<ReliableMessageTaskPageResponse> response = controller.queryOrderResultTasks(request);

        assertEquals(0, response.getCode());
        assertEquals(pageResponse, response.getData());
        verify(queryService).queryOrderResultTasks(request);
    }
}
