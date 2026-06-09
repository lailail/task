package com.example.ticket.job.support;

import com.example.ticket.job.service.ReservationRecheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预扣回查调度器。
 * 用于周期性触发预扣回查服务，补发因中间链路缺失而未完成的释放事件。
 */
@Component
public class ReservationRecheckScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReservationRecheckScheduler.class);

    private final ReservationRecheckService reservationRecheckService;

    /**
     * 构造预扣回查调度器。
     *
     * @param reservationRecheckService 预扣回查服务
     */
    public ReservationRecheckScheduler(ReservationRecheckService reservationRecheckService) {
        this.reservationRecheckService = reservationRecheckService;
    }

    /**
     * 按配置周期执行一次预扣回查。
     */
    @Scheduled(cron = "${ticket.job.reservation-recheck-cron}")
    public void run() {
        runOnce();
    }

    /**
     * 执行一次预扣回查。
     * 独立方法用于让测试直接验证调度入口与服务委托关系。
     */
    public void runOnce() {
        log.info("触发预扣回查扫描");
        reservationRecheckService.recheckExpiredReservations();
    }
}
