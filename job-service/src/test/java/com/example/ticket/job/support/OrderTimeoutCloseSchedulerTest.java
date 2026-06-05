package com.example.ticket.job.support;

import com.example.ticket.job.service.OrderTimeoutCloseService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 超时关单调度器单元测试。
 * 用于固定定时入口只负责触发超时关单服务，不承载额外业务逻辑。
 */
class OrderTimeoutCloseSchedulerTest {

    /**
     * 调度器触发时，应委托超时关单服务执行扫描。
     */
    @Test
    void should_delegate_to_order_timeout_close_service() {
        OrderTimeoutCloseService orderTimeoutCloseService = mock(OrderTimeoutCloseService.class);
        OrderTimeoutCloseScheduler scheduler = new OrderTimeoutCloseScheduler(orderTimeoutCloseService);

        scheduler.runOnce();

        verify(orderTimeoutCloseService).closeExpiredOrders();
    }
}
