package com.example.ticket.seckill.controller;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.seckill.service.OrderCreateTaskQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 下单请求补偿任务内部查询控制器测试。
 * 用于验证控制层只负责转交请求并返回统一响应包装。
 */
class OrderCreateTaskInternalControllerTest {

    @Test
    void should_return_wrapped_page_response_when_query_order_create_tasks() {
        OrderCreateTaskQueryService queryService = mock(OrderCreateTaskQueryService.class);
        OrderCreateTaskInternalController controller = new OrderCreateTaskInternalController(queryService);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();
        request.setTaskStatus("PENDING");
        ReliableMessageTaskPageResponse pageResponse = new ReliableMessageTaskPageResponse();
        pageResponse.setRecords(List.of());
        pageResponse.setCurrent(1L);
        pageResponse.setPageSize(10L);
        pageResponse.setTotal(0L);
        when(queryService.queryOrderCreateTasks(request)).thenReturn(pageResponse);

        ApiResponse<ReliableMessageTaskPageResponse> response = controller.queryOrderCreateTasks(request);

        assertEquals(0, response.getCode());
        assertEquals(pageResponse, response.getData());
        verify(queryService).queryOrderCreateTasks(request);
    }
}
