package com.example.ticket.job.service.impl;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.gateway.StockReleaseGateway;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.service.ReservationReleaseService;
import com.example.ticket.job.support.JobConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 预扣释放服务实现。
 * 用于消费库存释放事件后执行 Redis 回补，并在成功后把预扣记录推进到 RELEASED。
 */
@Service
public class ReservationReleaseServiceImpl implements ReservationReleaseService {
    private final JobReservationRecordMapper reservationRecordMapper;
    private final StockReleaseGateway stockReleaseGateway;

    /**
     * 构造预扣释放服务。
     *
     * @param reservationRecordMapper 预扣记录 Mapper
     * @param stockReleaseGateway 库存回补网关
     */
    public ReservationReleaseServiceImpl(
            JobReservationRecordMapper reservationRecordMapper,
            StockReleaseGateway stockReleaseGateway
    ) {
        this.reservationRecordMapper = reservationRecordMapper;
        this.stockReleaseGateway = stockReleaseGateway;
    }

    /**
     * 处理库存释放事件。
     *
     * @param event 库存释放事件
     */
    @Override
    @Transactional
    public void handleStockRelease(StockReleaseEvent event) {
        JobReservationRecordDO record = reservationRecordMapper.selectById(event.getReservationId());
        if (record == null) {
            return;
        }
        if (!canRelease(record)) {
            return;
        }
        if (!stockReleaseGateway.release(event)) {
            return;
        }
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_RELEASED);
        record.setReason(event.getReason());
        record.setReleasedAt(LocalDateTime.now());
        reservationRecordMapper.updateById(record);
    }

    /**
     * 判断当前预扣记录是否允许进入释放路径。
     * 当前阶段允许两类状态进入回补：
     * 1. `RESERVED`：建单失败后的直接释放
     * 2. `CONFIRMED`：订单超时关闭后的回补
     *
     * @param record 预扣记录
     * @return 是否允许释放
     */
    private boolean canRelease(JobReservationRecordDO record) {
        return JobConstants.RESERVATION_STATUS_RESERVED.equals(record.getReservationStatus())
                || JobConstants.RESERVATION_STATUS_CONFIRMED.equals(record.getReservationStatus());
    }
}
