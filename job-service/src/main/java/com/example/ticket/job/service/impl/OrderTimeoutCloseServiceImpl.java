package com.example.ticket.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobTicketOrderDO;
import com.example.ticket.job.gateway.StockReleaseEventPublisher;
import com.example.ticket.job.mapper.JobTicketOrderMapper;
import com.example.ticket.job.service.OrderTimeoutCloseService;
import com.example.ticket.job.support.JobConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 超时关单服务实现。
 * 用于扫描已过期的 `CREATED` 订单，把状态推进到 `CLOSED`，并在事务提交后发布超时释放事件。
 */
@Service
public class OrderTimeoutCloseServiceImpl implements OrderTimeoutCloseService {
    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutCloseServiceImpl.class);
    private static final long FIRST_PAGE_NO = 1L;

    private final JobTicketOrderMapper jobTicketOrderMapper;
    private final StockReleaseEventPublisher stockReleaseEventPublisher;
    private final int closeBatchSize;

    /**
     * 构造超时关单服务。
     *
     * @param jobTicketOrderMapper 订单 Mapper
     * @param stockReleaseEventPublisher 库存释放事件发布网关
     * @param closeBatchSize 单次扫描批量大小
     */
    public OrderTimeoutCloseServiceImpl(
            JobTicketOrderMapper jobTicketOrderMapper,
            StockReleaseEventPublisher stockReleaseEventPublisher,
            @Value("${ticket.job.order-timeout-close-batch-size}") int closeBatchSize
    ) {
        this.jobTicketOrderMapper = jobTicketOrderMapper;
        this.stockReleaseEventPublisher = stockReleaseEventPublisher;
        this.closeBatchSize = closeBatchSize;
    }

    /**
     * 使用当前时间执行一次超时关单扫描。
     */
    @Override
    @Transactional
    public void closeExpiredOrders() {
        closeExpiredOrders(LocalDateTime.now());
    }

    /**
     * 使用指定时间执行一次超时关单扫描。
     *
     * @param currentTime 当前扫描时间
     */
    @Override
    @Transactional
    public void closeExpiredOrders(LocalDateTime currentTime) {
        List<JobTicketOrderDO> expiredOrders = loadExpiredOrders(currentTime);
        log.info("开始扫描超时订单，closeTime={}, batchSize={}, actualSize={}", currentTime, closeBatchSize, expiredOrders.size());
        for (JobTicketOrderDO expiredOrder : expiredOrders) {
            closeSingleExpiredOrder(expiredOrder, currentTime);
        }
    }

    /**
     * 加载一批已过期且仍处于 `CREATED` 状态的订单。
     * 这里只扫描最小批次，避免当前阶段把定时任务做成无边界的全表处理。
     *
     * @param currentTime 当前扫描时间
     * @return 超时订单列表
     */
    private List<JobTicketOrderDO> loadExpiredOrders(LocalDateTime currentTime) {
        LambdaQueryWrapper<JobTicketOrderDO> queryWrapper = new LambdaQueryWrapper<JobTicketOrderDO>()
                .eq(JobTicketOrderDO::getOrderStatus, JobConstants.ORDER_STATUS_CREATED)
                .le(JobTicketOrderDO::getExpireAt, currentTime)
                .orderByAsc(JobTicketOrderDO::getExpireAt);
        Page<JobTicketOrderDO> page = new Page<>(FIRST_PAGE_NO, closeBatchSize);
        return jobTicketOrderMapper.selectPage(page, queryWrapper).getRecords();
    }

    /**
     * 关闭单笔超时订单并在成功后触发库存释放。
     * 只有订单状态落库成功后才允许继续发布释放事件，避免无效补偿。
     *
     * @param order 超时订单
     * @param currentTime 当前扫描时间
     */
    private void closeSingleExpiredOrder(JobTicketOrderDO order, LocalDateTime currentTime) {
        JobTicketOrderDO updateTarget = new JobTicketOrderDO();
        updateTarget.setOrderStatus(JobConstants.ORDER_STATUS_CLOSED);
        updateTarget.setClosedAt(currentTime);
        LambdaUpdateWrapper<JobTicketOrderDO> updateWrapper = new LambdaUpdateWrapper<JobTicketOrderDO>()
                .eq(JobTicketOrderDO::getOrderId, order.getOrderId())
                .eq(JobTicketOrderDO::getOrderStatus, JobConstants.ORDER_STATUS_CREATED);

        // 只有仍处于 CREATED 的订单才允许被当前批次关单，避免并发调度覆盖其他状态流转。
        if (jobTicketOrderMapper.update(updateTarget, updateWrapper) <= 0) {
            log.warn("超时关单命中并发竞争，跳过当前订单，orderId={}, reservationId={}, requestId={}", order.getOrderId(), order.getReservationId(), order.getRequestId());
            return;
        }

        publishAfterCommit(buildTimeoutReleaseEvent(order));
        log.info("超时订单已关闭并准备发布库存释放事件，orderId={}, reservationId={}, requestId={}", order.getOrderId(), order.getReservationId(), order.getRequestId());
    }

    /**
     * 构造订单超时释放事件。
     *
     * @param order 已成功关闭的超时订单
     * @return 库存释放事件
     */
    private StockReleaseEvent buildTimeoutReleaseEvent(JobTicketOrderDO order) {
        StockReleaseEvent event = new StockReleaseEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(StockEventConstants.ORDER_TIMEOUT_RELEASE);
        event.setOccurredAt(Instant.now());
        event.setRequestId(order.getRequestId());
        event.setIdempotencyKey(order.getIdempotencyKey());
        event.setReservationId(order.getReservationId());
        event.setOrderId(order.getOrderId());
        event.setActivityId(order.getActivityId());
        event.setTicketId(order.getTicketId());
        event.setUserId(order.getUserId());
        event.setQuantity(order.getQuantity());
        event.setReason(JobConstants.STOCK_RELEASE_REASON_ORDER_TIMEOUT);
        event.setSource(JobConstants.SOURCE_JOB_SERVICE);
        return event;
    }

    /**
     * 在事务提交后发布库存释放事件。
     * 这样可以保证只有订单状态已经稳定写入数据库后，才会触发后续 Redis 回补和预扣状态收敛。
     *
     * @param event 库存释放事件
     */
    private void publishAfterCommit(StockReleaseEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.info("库存释放事件直接发布，eventType={}, orderId={}, reservationId={}, requestId={}", event.getEventType(), event.getOrderId(), event.getReservationId(), event.getRequestId());
            stockReleaseEventPublisher.publish(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 在本地事务提交成功后发送库存释放事件。
             */
            @Override
            public void afterCommit() {
                log.info("库存释放事件在事务提交后发布，eventType={}, orderId={}, reservationId={}, requestId={}", event.getEventType(), event.getOrderId(), event.getReservationId(), event.getRequestId());
                stockReleaseEventPublisher.publish(event);
            }
        });
    }
}
