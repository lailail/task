package com.example.ticket.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.request.ReservationRecordQueryRequest;
import com.example.ticket.job.response.ReservationRecordPageResponse;
import com.example.ticket.job.service.impl.ReservationRecordQueryServiceImpl;
import com.example.ticket.job.support.JobConstants;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 预扣记录查询服务测试。
 * 用于验证分页边界归一化和响应字段映射，避免后台查询页分页语义漂移。
 */
class ReservationRecordQueryServiceTest {

    @Test
    void should_normalize_paging_and_map_page_response_when_query_reservation_records() {
        JobReservationRecordMapper mapper = mock(JobReservationRecordMapper.class);
        ReservationRecordQueryServiceImpl service = new ReservationRecordQueryServiceImpl(mapper);
        ReservationRecordQueryRequest request = new ReservationRecordQueryRequest();
        request.setCurrent(0L);
        request.setPageSize(1000L);
        request.setReservationStatus(JobConstants.RESERVATION_STATUS_RESERVED);

        JobReservationRecordDO record = new JobReservationRecordDO();
        record.setReservationId("r-1001");
        record.setRequestId("req-1001");
        record.setUserId(1L);
        record.setOrderId(2L);
        record.setActivityId(3L);
        record.setTicketId(4L);
        record.setQuantity(2);
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_RESERVED);
        record.setSource(JobConstants.SOURCE_JOB_SERVICE);
        record.setReason("DEMO");
        record.setExpireAt(LocalDateTime.of(2026, 6, 10, 12, 0));
        record.setReleasedAt(LocalDateTime.of(2026, 6, 10, 12, 30));

        Page<JobReservationRecordDO> mapperPage = new Page<>(1L, JobConstants.MAX_PAGE_SIZE);
        mapperPage.setRecords(List.of(record));
        mapperPage.setTotal(1L);
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        ReservationRecordPageResponse response = service.queryReservationRecords(request);

        ArgumentCaptor<Page<JobReservationRecordDO>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<LambdaQueryWrapper<JobReservationRecordDO>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertEquals(JobConstants.DEFAULT_PAGE_CURRENT, pageCaptor.getValue().getCurrent());
        assertEquals(JobConstants.MAX_PAGE_SIZE, pageCaptor.getValue().getSize());
        assertNotNull(wrapperCaptor.getValue());

        assertEquals(1L, response.getTotal());
        assertEquals(1L, response.getCurrent());
        assertEquals(JobConstants.MAX_PAGE_SIZE, response.getPageSize());
        assertEquals(1, response.getRecords().size());
        assertEquals("r-1001", response.getRecords().get(0).getReservationId());
        assertEquals(2L, response.getRecords().get(0).getOrderId());
    }
}
