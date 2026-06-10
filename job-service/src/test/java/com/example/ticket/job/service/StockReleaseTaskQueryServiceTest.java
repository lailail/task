package com.example.ticket.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageConstants;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.reliable.query.ReliableMessageTaskTypes;
import com.example.ticket.job.domain.JobStockReleaseTaskDO;
import com.example.ticket.job.mapper.JobStockReleaseTaskMapper;
import com.example.ticket.job.service.impl.StockReleaseTaskQueryServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存释放补偿任务查询服务测试。
 * 用于验证分页边界归一化以及 eventId 到统一 eventKey 语义的映射保持稳定。
 */
class StockReleaseTaskQueryServiceTest {

    @Test
    void should_normalize_paging_and_map_stock_release_tasks() {
        JobStockReleaseTaskMapper mapper = mock(JobStockReleaseTaskMapper.class);
        StockReleaseTaskQueryServiceImpl service = new StockReleaseTaskQueryServiceImpl(mapper);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();
        request.setCurrent(0L);
        request.setPageSize(999L);
        request.setEventKey("evt-stock-release-1");

        JobStockReleaseTaskDO record = new JobStockReleaseTaskDO();
        record.setTaskId(31L);
        record.setEventId("evt-stock-release-1");
        record.setEventType("ticket.stock.release");
        record.setBusinessKey("reservation-1");
        record.setTaskStatus("PENDING");

        Page<JobStockReleaseTaskDO> mapperPage = new Page<>(1L, ReliableMessageTaskPageConstants.MAX_PAGE_SIZE);
        mapperPage.setRecords(List.of(record));
        mapperPage.setTotal(1L);
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        ReliableMessageTaskPageResponse response = service.queryStockReleaseTasks(request);

        ArgumentCaptor<Page<JobStockReleaseTaskDO>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(mapper).selectPage(pageCaptor.capture(), any(LambdaQueryWrapper.class));
        assertEquals(ReliableMessageTaskPageConstants.DEFAULT_PAGE_CURRENT, pageCaptor.getValue().getCurrent());
        assertEquals(ReliableMessageTaskPageConstants.MAX_PAGE_SIZE, pageCaptor.getValue().getSize());
        assertEquals(ReliableMessageTaskTypes.STOCK_RELEASE, response.getRecords().get(0).getTaskType());
        assertEquals("evt-stock-release-1", response.getRecords().get(0).getEventKey());
    }
}
