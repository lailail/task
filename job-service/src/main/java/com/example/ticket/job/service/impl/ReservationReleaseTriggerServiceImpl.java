package com.example.ticket.job.service.impl;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.gateway.StockReleaseEventPublisher;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.service.ReservationReleaseTriggerService;
import com.example.ticket.job.support.JobConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

/**
 * 预扣释放触发服务实现。
 * 用于在收到建单失败结果后，识别仍处于 RESERVED 的预扣记录，并异步发出库存释放事件。
 */
@Service
public class ReservationReleaseTriggerServiceImpl implements ReservationReleaseTriggerService {
    private static final Logger log = LoggerFactory.getLogger(ReservationReleaseTriggerServiceImpl.class);

    private final JobReservationRecordMapper reservationRecordMapper;
    private final StockReleaseEventPublisher stockReleaseEventPublisher;

    /**
     * 构造预扣释放触发服务。
     *
     * @param reservationRecordMapper 预扣记录 Mapper
     * @param stockReleaseEventPublisher 库存释放事件发布网关
     */
    public ReservationReleaseTriggerServiceImpl(
            JobReservationRecordMapper reservationRecordMapper,
            StockReleaseEventPublisher stockReleaseEventPublisher
    ) {
        this.reservationRecordMapper = reservationRecordMapper;
        this.stockReleaseEventPublisher = stockReleaseEventPublisher;
    }

    /**
     * 处理下单结果事件。
     *
     * @param event 下单结果事件
     */
    @Override
    @Transactional
    public void handleOrderCreateResult(OrderCreateResultEvent event) {
        if (!JobConstants.ORDER_RESULT_TYPE_CREATE_FAILED.equals(event.getEventType())) {
            log.debug("订单结果事件不是建单失败，跳过释放触发，eventId={}, eventType={}", event.getEventId(), event.getEventType());
            return;
        }
        JobReservationRecordDO record = reservationRecordMapper.selectById(event.getReservationId());
        if (record == null) {
            log.warn("建单失败释放触发未找到预扣记录，eventId={}, reservationId={}, requestId={}", event.getEventId(), event.getReservationId(), event.getRequestId());
            return;
        }
        if (!JobConstants.RESERVATION_STATUS_RESERVED.equals(record.getReservationStatus())) {
            log.warn("建单失败释放触发跳过非法状态记录，eventId={}, reservationId={}, currentStatus={}", event.getEventId(), event.getReservationId(), record.getReservationStatus());
            return;
        }
        publishAfterCommit(buildReleaseEvent(event));
        log.info("建单失败后已准备发布库存释放事件，eventId={}, reservationId={}, requestId={}", event.getEventId(), event.getReservationId(), event.getRequestId());
    }

    /**
     * 构造库存释放事件。
     *
     * @param event 下单结果事件
     * @return 库存释放事件
     */
    private StockReleaseEvent buildReleaseEvent(OrderCreateResultEvent event) {
        StockReleaseEvent releaseEvent = new StockReleaseEvent();
        releaseEvent.setEventId(UUID.randomUUID().toString());
        releaseEvent.setEventType(StockEventConstants.ORDER_CREATE_FAILED_RELEASE);
        releaseEvent.setOccurredAt(Instant.now());
        releaseEvent.setRequestId(event.getRequestId());
        releaseEvent.setIdempotencyKey(event.getIdempotencyKey());
        releaseEvent.setReservationId(event.getReservationId());
        releaseEvent.setOrderId(event.getOrderId());
        releaseEvent.setActivityId(event.getActivityId());
        releaseEvent.setTicketId(event.getTicketId());
        releaseEvent.setUserId(event.getUserId());
        releaseEvent.setQuantity(event.getQuantity());
        releaseEvent.setReason(event.getReason());
        releaseEvent.setSource(JobConstants.SOURCE_JOB_SERVICE);
        return releaseEvent;
    }

    /**
     * 在事务提交后发布库存释放事件。
     * 这样可以避免数据库侧判断尚未稳定时就提前触发释放补偿。
     *
     * @param event 库存释放事件
     */
    private void publishAfterCommit(StockReleaseEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.info("库存释放事件直接发布，eventType={}, reservationId={}, requestId={}", event.getEventType(), event.getReservationId(), event.getRequestId());
            stockReleaseEventPublisher.publish(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 在本地事务成功提交后再发送库存释放事件。
             */
            @Override
            public void afterCommit() {
                log.info("库存释放事件在事务提交后发布，eventType={}, reservationId={}, requestId={}", event.getEventType(), event.getReservationId(), event.getRequestId());
                stockReleaseEventPublisher.publish(event);
            }
        });
    }
}
