package com.example.ticket.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobTicketOrderDO;
import com.example.ticket.job.gateway.StockReleaseEventPublisher;
import com.example.ticket.job.mapper.JobTicketOrderMapper;
import com.example.ticket.job.service.impl.OrderTimeoutCloseServiceImpl;
import com.example.ticket.job.support.JobConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 超时关单服务单元测试。
 * 用于固定“扫描到超时未支付订单后，应关闭订单并发布超时释放事件”的业务边界。
 */
@ExtendWith(MockitoExtension.class)
class OrderTimeoutCloseServiceTest {

    @Mock
    private JobTicketOrderMapper jobTicketOrderMapper;

    @Mock
    private StockReleaseEventPublisher stockReleaseEventPublisher;

    private OrderTimeoutCloseService orderTimeoutCloseService;

    /**
     * 构造被测超时关单服务。
     * 当前测试通过 mock 订单 Mapper 和释放事件发布网关，隔离超时关单编排规则。
     */
    @BeforeEach
    void setUp() {
        orderTimeoutCloseService = new OrderTimeoutCloseServiceImpl(
                jobTicketOrderMapper,
                stockReleaseEventPublisher,
                100
        );
    }

    /**
     * 扫描到超时未支付订单时，应关闭订单并发布超时释放事件。
     */
    @Test
    void should_close_expired_created_order_and_publish_timeout_release_event() {
        JobTicketOrderDO order = buildExpiredCreatedOrder();
        when(jobTicketOrderMapper.selectPage(any(Page.class), any())).thenReturn(new Page<JobTicketOrderDO>()
                .setRecords(List.of(order)));
        when(jobTicketOrderMapper.update(any(JobTicketOrderDO.class), any())).thenReturn(1);

        orderTimeoutCloseService.closeExpiredOrders(LocalDateTime.of(2026, 6, 5, 15, 30, 0));

        ArgumentCaptor<JobTicketOrderDO> orderCaptor = ArgumentCaptor.forClass(JobTicketOrderDO.class);
        verify(jobTicketOrderMapper).update(orderCaptor.capture(), any());
        assertEquals(JobConstants.ORDER_STATUS_CLOSED, orderCaptor.getValue().getOrderStatus());
        assertNotNull(orderCaptor.getValue().getClosedAt());

        ArgumentCaptor<StockReleaseEvent> eventCaptor = ArgumentCaptor.forClass(StockReleaseEvent.class);
        verify(stockReleaseEventPublisher).publish(eventCaptor.capture());
        assertEquals(StockEventConstants.ORDER_TIMEOUT_RELEASE, eventCaptor.getValue().getEventType());
        assertEquals("reservation-001", eventCaptor.getValue().getReservationId());
        assertEquals("ORDER_TIMEOUT", eventCaptor.getValue().getReason());
    }

    /**
     * 如果订单更新失败，则不应继续发布超时释放事件，避免重复补偿。
     */
    @Test
    void should_not_publish_timeout_release_event_when_order_update_failed() {
        JobTicketOrderDO order = buildExpiredCreatedOrder();
        when(jobTicketOrderMapper.selectPage(any(Page.class), any())).thenReturn(new Page<JobTicketOrderDO>()
                .setRecords(List.of(order)));
        when(jobTicketOrderMapper.update(any(JobTicketOrderDO.class), any())).thenReturn(0);

        orderTimeoutCloseService.closeExpiredOrders(LocalDateTime.of(2026, 6, 5, 15, 30, 0));

        verify(stockReleaseEventPublisher, never()).publish(any(StockReleaseEvent.class));
    }

    /**
     * 构造超时未支付订单。
     *
     * @return 订单对象
     */
    private JobTicketOrderDO buildExpiredCreatedOrder() {
        JobTicketOrderDO order = new JobTicketOrderDO();
        order.setOrderId(20001L);
        order.setReservationId("reservation-001");
        order.setRequestId("req-001");
        order.setIdempotencyKey("idem-001");
        order.setUserId(10001L);
        order.setActivityId(1001L);
        order.setTicketId(501L);
        order.setQuantity(1);
        order.setOrderStatus(JobConstants.ORDER_STATUS_CREATED);
        order.setExpireAt(LocalDateTime.of(2026, 6, 5, 15, 0, 0));
        return order;
    }
}
