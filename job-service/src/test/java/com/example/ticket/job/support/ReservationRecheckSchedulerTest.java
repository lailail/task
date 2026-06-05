package com.example.ticket.job.support;

import com.example.ticket.job.service.ReservationRecheckService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 预扣回查调度器单元测试。
 * 用于固定调度入口只负责委托回查服务执行，不承载额外业务逻辑。
 */
class ReservationRecheckSchedulerTest {

    /**
     * 调度器触发时，应委托预扣回查服务执行扫描。
     */
    @Test
    void should_delegate_to_reservation_recheck_service() {
        ReservationRecheckService reservationRecheckService = mock(ReservationRecheckService.class);
        ReservationRecheckScheduler scheduler = new ReservationRecheckScheduler(reservationRecheckService);

        scheduler.runOnce();

        verify(reservationRecheckService).recheckExpiredReservations();
    }
}
