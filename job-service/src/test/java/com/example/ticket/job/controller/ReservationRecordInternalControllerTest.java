package com.example.ticket.job.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.job.request.ReservationRecordQueryRequest;
import com.example.ticket.job.response.ReservationRecordPageResponse;
import com.example.ticket.job.service.ReservationRecordQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 预扣记录内部查询控制器测试。
 * 用于验证控制层只负责转交请求并返回统一响应包装。
 */
class ReservationRecordInternalControllerTest {

    @Test
    void should_return_wrapped_page_response_when_query_reservation_records() {
        ReservationRecordQueryService queryService = mock(ReservationRecordQueryService.class);
        ReservationRecordInternalController controller = new ReservationRecordInternalController(queryService);
        ReservationRecordQueryRequest request = new ReservationRecordQueryRequest();
        request.setReservationStatus("RESERVED");
        ReservationRecordPageResponse pageResponse = new ReservationRecordPageResponse();
        pageResponse.setRecords(List.of());
        pageResponse.setCurrent(1L);
        pageResponse.setPageSize(10L);
        pageResponse.setTotal(0L);
        when(queryService.queryReservationRecords(request)).thenReturn(pageResponse);

        ApiResponse<ReservationRecordPageResponse> response = controller.queryReservationRecords(request);

        assertEquals(0, response.getCode());
        assertEquals(pageResponse, response.getData());
        verify(queryService).queryReservationRecords(request);
    }
}
