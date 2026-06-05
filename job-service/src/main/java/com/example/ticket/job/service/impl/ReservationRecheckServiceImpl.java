package com.example.ticket.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.domain.JobTicketOrderDO;
import com.example.ticket.job.gateway.StockReleaseEventPublisher;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.mapper.JobTicketOrderMapper;
import com.example.ticket.job.service.ReservationRecheckService;
import com.example.ticket.job.support.JobConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 预扣回查服务实现。
 * 用于扫描超时未收敛的预扣记录，并根据关联订单事实决定是否补发库存释放事件。
 */
@Service
public class ReservationRecheckServiceImpl implements ReservationRecheckService {
    private static final long FIRST_PAGE_NO = 1L;

    private final JobReservationRecordMapper reservationRecordMapper;
    private final JobTicketOrderMapper jobTicketOrderMapper;
    private final StockReleaseEventPublisher stockReleaseEventPublisher;
    private final int recheckBatchSize;

    /**
     * 构造预扣回查服务。
     *
     * @param reservationRecordMapper 预扣记录 Mapper
     * @param jobTicketOrderMapper 订单 Mapper
     * @param stockReleaseEventPublisher 库存释放事件发布网关
     * @param recheckBatchSize 单次回查批量大小
     */
    public ReservationRecheckServiceImpl(
            JobReservationRecordMapper reservationRecordMapper,
            JobTicketOrderMapper jobTicketOrderMapper,
            StockReleaseEventPublisher stockReleaseEventPublisher,
            @Value("${ticket.job.reservation-recheck-batch-size}") int recheckBatchSize
    ) {
        this.reservationRecordMapper = reservationRecordMapper;
        this.jobTicketOrderMapper = jobTicketOrderMapper;
        this.stockReleaseEventPublisher = stockReleaseEventPublisher;
        this.recheckBatchSize = recheckBatchSize;
    }

    /**
     * 使用当前时间执行一次预扣回查。
     */
    @Override
    public void recheckExpiredReservations() {
        recheckExpiredReservations(LocalDateTime.now());
    }

    /**
     * 使用指定时间执行一次预扣回查。
     *
     * @param currentTime 当前回查时间
     */
    @Override
    public void recheckExpiredReservations(LocalDateTime currentTime) {
        List<JobReservationRecordDO> records = loadExpiredReservations(currentTime);
        for (JobReservationRecordDO record : records) {
            recheckSingleReservation(record);
        }
    }

    /**
     * 加载一批已过期且仍未收敛的预扣记录。
     * 当前阶段只扫描 `RESERVED` 和 `CONFIRMED`，避免把已释放记录反复拉入补偿链路。
     *
     * @param currentTime 当前回查时间
     * @return 预扣记录列表
     */
    private List<JobReservationRecordDO> loadExpiredReservations(LocalDateTime currentTime) {
        LambdaQueryWrapper<JobReservationRecordDO> queryWrapper = new LambdaQueryWrapper<JobReservationRecordDO>()
                .in(JobReservationRecordDO::getReservationStatus,
                        JobConstants.RESERVATION_STATUS_RESERVED,
                        JobConstants.RESERVATION_STATUS_CONFIRMED)
                .le(JobReservationRecordDO::getExpireAt, currentTime)
                .orderByAsc(JobReservationRecordDO::getExpireAt);
        Page<JobReservationRecordDO> page = new Page<>(FIRST_PAGE_NO, recheckBatchSize);
        return reservationRecordMapper.selectPage(page, queryWrapper).getRecords();
    }

    /**
     * 回查单条预扣记录。
     * 当前只处理两种明确安全的场景：
     * 1. `RESERVED` 且不存在订单事实，说明建单链路未收敛，可补发失败释放
     * 2. `CONFIRMED` 且订单已关闭，说明超时关单后的释放链路需要补发
     *
     * @param record 预扣记录
     */
    private void recheckSingleReservation(JobReservationRecordDO record) {
        JobTicketOrderDO order = loadOrderByReservationId(record.getReservationId());
        if (shouldReleaseReservedWithoutOrder(record, order)) {
            stockReleaseEventPublisher.publish(buildCreateFailedReleaseEvent(record));
            return;
        }
        if (shouldReleaseClosedConfirmedOrder(record, order)) {
            stockReleaseEventPublisher.publish(buildTimeoutReleaseEvent(record, order));
        }
    }

    /**
     * 按预扣标识加载关联订单。
     *
     * @param reservationId 预扣标识
     * @return 关联订单，不存在时返回空
     */
    private JobTicketOrderDO loadOrderByReservationId(String reservationId) {
        LambdaQueryWrapper<JobTicketOrderDO> queryWrapper = new LambdaQueryWrapper<JobTicketOrderDO>()
                .eq(JobTicketOrderDO::getReservationId, reservationId);
        Page<JobTicketOrderDO> page = new Page<>(FIRST_PAGE_NO, 1);
        List<JobTicketOrderDO> orders = jobTicketOrderMapper.selectPage(page, queryWrapper).getRecords();
        if (orders.isEmpty()) {
            return null;
        }
        return orders.get(0);
    }

    /**
     * 判断是否应把“超时无订单”的 RESERVED 记录补发为失败释放。
     *
     * @param record 预扣记录
     * @param order 关联订单
     * @return 是否补发失败释放
     */
    private boolean shouldReleaseReservedWithoutOrder(JobReservationRecordDO record, JobTicketOrderDO order) {
        return JobConstants.RESERVATION_STATUS_RESERVED.equals(record.getReservationStatus())
                && order == null;
    }

    /**
     * 判断是否应把“已关单但未释放”的 CONFIRMED 记录补发为超时释放。
     *
     * @param record 预扣记录
     * @param order 关联订单
     * @return 是否补发超时释放
     */
    private boolean shouldReleaseClosedConfirmedOrder(JobReservationRecordDO record, JobTicketOrderDO order) {
        return (JobConstants.RESERVATION_STATUS_CONFIRMED.equals(record.getReservationStatus())
                || JobConstants.RESERVATION_STATUS_RESERVED.equals(record.getReservationStatus()))
                && order != null
                && JobConstants.ORDER_STATUS_CLOSED.equals(order.getOrderStatus());
    }

    /**
     * 构造无订单事实的失败释放事件。
     *
     * @param record 预扣记录
     * @return 库存释放事件
     */
    private StockReleaseEvent buildCreateFailedReleaseEvent(JobReservationRecordDO record) {
        StockReleaseEvent event = buildBaseEvent(record);
        event.setEventType(StockEventConstants.ORDER_CREATE_FAILED_RELEASE);
        event.setReason(JobConstants.STOCK_RELEASE_REASON_RESERVATION_RECHECK_EXPIRED);
        return event;
    }

    /**
     * 构造订单已关闭场景的超时释放事件。
     *
     * @param record 预扣记录
     * @param order 关联订单
     * @return 库存释放事件
     */
    private StockReleaseEvent buildTimeoutReleaseEvent(JobReservationRecordDO record, JobTicketOrderDO order) {
        StockReleaseEvent event = buildBaseEvent(record);
        event.setEventType(StockEventConstants.ORDER_TIMEOUT_RELEASE);
        event.setOrderId(order.getOrderId());
        event.setReason(JobConstants.STOCK_RELEASE_REASON_ORDER_TIMEOUT);
        return event;
    }

    /**
     * 构造库存释放事件基础字段。
     *
     * @param record 预扣记录
     * @return 库存释放事件
     */
    private StockReleaseEvent buildBaseEvent(JobReservationRecordDO record) {
        StockReleaseEvent event = new StockReleaseEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setOccurredAt(Instant.now());
        event.setRequestId(record.getRequestId());
        event.setIdempotencyKey(record.getIdempotencyKey());
        event.setReservationId(record.getReservationId());
        event.setOrderId(record.getOrderId());
        event.setActivityId(record.getActivityId());
        event.setTicketId(record.getTicketId());
        event.setUserId(record.getUserId());
        event.setQuantity(record.getQuantity());
        event.setSource(JobConstants.SOURCE_JOB_SERVICE);
        return event;
    }
}
