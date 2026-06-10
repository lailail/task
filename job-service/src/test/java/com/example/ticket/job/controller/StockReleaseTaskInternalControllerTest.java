package com.example.ticket.job.controller;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.job.service.StockReleaseTaskQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存释放补偿任务内部查询控制器测试。
 * 用于验证控制层只负责转交请求并返回统一响应包装。
 */
class StockReleaseTaskInternalControllerTest {

    @Test
    void should_return_wrapped_page_response_when_query_stock_release_tasks() {
        StockReleaseTaskQueryService queryService = mock(StockReleaseTaskQueryService.class);
        StockReleaseTaskInternalController controller = new StockReleaseTaskInternalController(queryService);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();
        ReliableMessageTaskPageResponse pageResponse = new ReliableMessageTaskPageResponse();
        pageResponse.setRecords(List.of());
        when(queryService.queryStockReleaseTasks(request)).thenReturn(pageResponse);

        ApiResponse<ReliableMessageTaskPageResponse> response = controller.queryStockReleaseTasks(request);

        assertEquals(0, response.getCode());
        assertEquals(pageResponse, response.getData());
        verify(queryService).queryStockReleaseTasks(request);
    }
}
