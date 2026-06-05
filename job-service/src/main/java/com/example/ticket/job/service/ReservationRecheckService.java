package com.example.ticket.job.service;

import java.time.LocalDateTime;

/**
 * 预扣回查服务。
 * 用于扫描超时未收敛的预扣记录，并根据订单事实补发库存释放事件。
 */
public interface ReservationRecheckService {

    /**
     * 使用当前时间执行一次预扣回查。
     */
    void recheckExpiredReservations();

    /**
     * 使用指定时间执行一次预扣回查。
     *
     * @param currentTime 当前回查时间
     */
    void recheckExpiredReservations(LocalDateTime currentTime);
}
