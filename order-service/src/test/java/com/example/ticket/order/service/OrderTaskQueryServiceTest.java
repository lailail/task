package com.example.ticket.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageConstants;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.reliable.query.ReliableMessageTaskTypes;
import com.example.ticket.order.domain.OrderCompleteTaskDO;
import com.example.ticket.order.domain.OrderResultTaskDO;
import com.example.ticket.order.mapper.OrderCompleteTaskMapper;
import com.example.ticket.order.mapper.OrderResultTaskMapper;
import com.example.ticket.order.service.impl.OrderTaskQueryServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单域补偿任务查询服务测试。
 * 用于验证分页边界归一化和两类订单补偿任务的统一字段映射。
 */
class OrderTaskQueryServiceTest {

    @Test
    void should_normalize_paging_and_map_order_result_tasks() {
        OrderResultTaskMapper resultMapper = mock(OrderResultTaskMapper.class);
        OrderCompleteTaskMapper completeTaskMapper = mock(OrderCompleteTaskMapper.class);
        OrderTaskQueryServiceImpl service = new OrderTaskQueryServiceImpl(resultMapper, completeTaskMapper);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();
        request.setCurrent(0L);
        request.setPageSize(999L);
        request.setTaskStatus("PENDING");

        OrderResultTaskDO record = new OrderResultTaskDO();
        record.setTaskId(11L);
        record.setEventKey("evt-order-result-1");
        record.setEventType("ticket.order.result");
        record.setBusinessKey("order-1");
        record.setTaskStatus("PENDING");
        record.setRetryCount(1);
        record.setMaxRetryCount(5);
        record.setNextRetryAt(LocalDateTime.of(2026, 6, 10, 10, 0));

        Page<OrderResultTaskDO> mapperPage = new Page<>(1L, ReliableMessageTaskPageConstants.MAX_PAGE_SIZE);
        mapperPage.setRecords(List.of(record));
        mapperPage.setTotal(1L);
        when(resultMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        ReliableMessageTaskPageResponse response = service.queryOrderResultTasks(request);

        ArgumentCaptor<Page<OrderResultTaskDO>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(resultMapper).selectPage(pageCaptor.capture(), any(LambdaQueryWrapper.class));
        assertEquals(ReliableMessageTaskPageConstants.DEFAULT_PAGE_CURRENT, pageCaptor.getValue().getCurrent());
        assertEquals(ReliableMessageTaskPageConstants.MAX_PAGE_SIZE, pageCaptor.getValue().getSize());
        assertEquals(ReliableMessageTaskTypes.ORDER_RESULT, response.getRecords().get(0).getTaskType());
    }

    @Test
    void should_map_order_complete_tasks() {
        OrderResultTaskMapper resultMapper = mock(OrderResultTaskMapper.class);
        OrderCompleteTaskMapper completeTaskMapper = mock(OrderCompleteTaskMapper.class);
        OrderTaskQueryServiceImpl service = new OrderTaskQueryServiceImpl(resultMapper, completeTaskMapper);
        ReliableMessageTaskQueryRequest request = new ReliableMessageTaskQueryRequest();
        request.setCurrent(1L);
        request.setPageSize(10L);

        OrderCompleteTaskDO record = new OrderCompleteTaskDO();
        record.setTaskId(21L);
        record.setEventKey("evt-order-complete-1");
        record.setEventType("ticket.order.completed");
        record.setBusinessKey("order-2");
        record.setTaskStatus("RETRYING");

        Page<OrderCompleteTaskDO> mapperPage = new Page<>(1L, 10L);
        mapperPage.setRecords(List.of(record));
        mapperPage.setTotal(1L);
        when(completeTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        ReliableMessageTaskPageResponse response = service.queryOrderCompleteTasks(request);

        assertEquals(1L, response.getTotal());
        assertEquals(ReliableMessageTaskTypes.ORDER_COMPLETE, response.getRecords().get(0).getTaskType());
        assertEquals("evt-order-complete-1", response.getRecords().get(0).getEventKey());
    }
}
