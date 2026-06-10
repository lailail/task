package com.example.ticket.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageConstants;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.reliable.query.ReliableMessageTaskTypes;
import com.example.ticket.seckill.domain.OrderCreateTaskDO;
import com.example.ticket.seckill.mapper.OrderCreateTaskMapper;
import com.example.ticket.seckill.service.impl.OrderCreateTaskQueryServiceImpl;
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
 * 下单请求补偿任务查询服务测试。
 * 用于验证分页边界归一化和统一响应字段映射，避免后台治理页读取到漂移数据。
 */
class OrderCreateTaskQueryServiceTest {

    @Test
    void should_normalize_paging_and_map_page_response_when_query_order_create_tasks() {
        OrderCreateTaskMapper mapper = mock(OrderCreateTaskMapper.class);
        OrderCreateTaskQueryServiceImpl service = new OrderCreateTaskQueryServiceImpl(mapper);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();
        request.setCurrent(0L);
        request.setPageSize(1000L);
        request.setTaskStatus("PENDING");

        OrderCreateTaskDO record = new OrderCreateTaskDO();
        record.setTaskId(1L);
        record.setEventKey("evt-order-create-1");
        record.setEventType("ticket.order.create");
        record.setBusinessKey("biz-1");
        record.setTaskStatus("PENDING");
        record.setRetryCount(1);
        record.setMaxRetryCount(5);
        record.setNextRetryAt(LocalDateTime.of(2026, 6, 10, 10, 0));
        record.setLastSentAt(LocalDateTime.of(2026, 6, 10, 9, 0));
        record.setLastErrorMessage("mock-error");

        Page<OrderCreateTaskDO> mapperPage =
                new Page<>(1L, ReliableMessageTaskPageConstants.MAX_PAGE_SIZE);
        mapperPage.setRecords(List.of(record));
        mapperPage.setTotal(1L);
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        ReliableMessageTaskPageResponse response = service.queryOrderCreateTasks(request);

        ArgumentCaptor<Page<OrderCreateTaskDO>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<LambdaQueryWrapper<OrderCreateTaskDO>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertEquals(ReliableMessageTaskPageConstants.DEFAULT_PAGE_CURRENT, pageCaptor.getValue().getCurrent());
        assertEquals(ReliableMessageTaskPageConstants.MAX_PAGE_SIZE, pageCaptor.getValue().getSize());
        assertNotNull(wrapperCaptor.getValue());

        assertEquals(1L, response.getTotal());
        assertEquals(1L, response.getCurrent());
        assertEquals(ReliableMessageTaskPageConstants.MAX_PAGE_SIZE, response.getPageSize());
        assertEquals(1, response.getRecords().size());
        assertEquals(ReliableMessageTaskTypes.ORDER_CREATE, response.getRecords().get(0).getTaskType());
        assertEquals("evt-order-create-1", response.getRecords().get(0).getEventKey());
    }
}
