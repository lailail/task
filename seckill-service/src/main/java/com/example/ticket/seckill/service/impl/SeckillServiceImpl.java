package com.example.ticket.seckill.service.impl;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.seckill.domain.ReservationDO;
import com.example.ticket.seckill.dto.SeckillActivityDTO;
import com.example.ticket.seckill.gateway.OrderCreateEventPublisher;
import com.example.ticket.seckill.gateway.StockReservationGateway;
import com.example.ticket.seckill.gateway.model.StockReserveCommand;
import com.example.ticket.seckill.gateway.model.StockReserveResult;
import com.example.ticket.seckill.repository.ReservationRecordRepository;
import com.example.ticket.seckill.repository.SeckillActivityRepository;
import com.example.ticket.seckill.request.SeckillReserveRequest;
import com.example.ticket.seckill.response.SeckillReserveResponse;
import com.example.ticket.seckill.service.SeckillService;
import com.example.ticket.seckill.support.SeckillConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * 抢票服务实现。
 * 当前负责编排活动校验、防重预扣和下单事件发送，是 Phase 3 第一段的业务收口。
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    private final SeckillActivityRepository activityRepository;
    private final StockReservationGateway stockReservationGateway;
    private final ReservationRecordRepository reservationRecordRepository;
    private final OrderCreateEventPublisher orderCreateEventPublisher;
    private final long reservationExpireSeconds;

    /**
     * 构造抢票服务实现。
     *
     * @param activityRepository 抢票活动仓储
     * @param stockReservationGateway 库存预扣缓存网关
     * @param reservationRecordRepository 预扣记录仓储
     * @param orderCreateEventPublisher 下单事件发布网关
     * @param reservationExpireSeconds 预扣过期秒数
     */
    public SeckillServiceImpl(
            SeckillActivityRepository activityRepository,
            StockReservationGateway stockReservationGateway,
            ReservationRecordRepository reservationRecordRepository,
            OrderCreateEventPublisher orderCreateEventPublisher,
            @Value("${ticket.seckill.reservation-expire-seconds:900}") long reservationExpireSeconds
    ) {
        this.activityRepository = activityRepository;
        this.stockReservationGateway = stockReservationGateway;
        this.reservationRecordRepository = reservationRecordRepository;
        this.orderCreateEventPublisher = orderCreateEventPublisher;
        this.reservationExpireSeconds = reservationExpireSeconds;
    }

    /**
     * 执行抢票预扣。
     *
     * @param authenticatedUser 已认证用户
     * @param request 抢票预扣请求
     * @return 预扣结果
     */
    @Override
    public SeckillReserveResponse reserve(AuthenticatedUser authenticatedUser, SeckillReserveRequest request) {
        SeckillActivityDTO activity = activityRepository.findByActivityIdAndTicketId(request.getActivityId(), request.getTicketId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SECKILL_ACTIVITY_NOT_FOUND));

        if (!SeckillConstants.SALE_STATUS_ON_SALE.equals(activity.getSaleStatus())) {
            throw new BusinessException(ErrorCode.SECKILL_ACTIVITY_NOT_ON_SALE);
        }

        ReservationDO reservation = buildReservation(authenticatedUser, request);
        StockReserveResult reserveResult = stockReservationGateway.reserve(buildStockReserveCommand(reservation));
        if (!SeckillConstants.RESERVE_RESULT_SUCCESS.equals(reserveResult.getResultCode())) {
            throw mapReserveException(reserveResult);
        }

        // 预扣成功后统一以缓存层确认的预扣标识为准，避免后续事件和查询链路出现标识漂移。
        reservation.setReservationId(reserveResult.getReservationId());
        reservation.setOccurredAt(reserveResult.getOccurredAt());
        reservation.setExpireAt(reserveResult.getExpireAt());

        // 当前阶段先保证“没有正式预扣记录就不发下单消息”，避免订单成功后 job-service 无法确认预扣状态。
        // 这里仍然存在 Redis 已预扣但数据库落库失败的一致性空窗，当前接受该过渡实现，并通过后续回查补偿继续收敛。
        reservationRecordRepository.save(reservation);
        orderCreateEventPublisher.publish(buildOrderCreateEvent(reservation));
        return buildReserveResponse(reservation);
    }

    /**
     * 构造预扣记录。
     *
     * @param authenticatedUser 已认证用户
     * @param request 抢票请求
     * @return 预扣记录
     */
    private ReservationDO buildReservation(AuthenticatedUser authenticatedUser, SeckillReserveRequest request) {
        ReservationDO reservation = new ReservationDO();
        reservation.setReservationId(UUID.randomUUID().toString());
        reservation.setUserId(authenticatedUser.getUserId());
        reservation.setActivityId(request.getActivityId());
        reservation.setTicketId(request.getTicketId());
        reservation.setQuantity(request.getQuantity());
        reservation.setStatus(SeckillConstants.RESERVATION_STATUS_RESERVED);
        reservation.setIdempotencyKey(request.getIdempotencyKey());
        reservation.setRequestId(request.getRequestId());
        reservation.setOccurredAt(Instant.now());
        reservation.setExpireAt(reservation.getOccurredAt().plusSeconds(reservationExpireSeconds));
        return reservation;
    }

    /**
     * 构造库存预扣命令。
     *
     * @param reservation 预扣记录
     * @return 库存预扣命令
     */
    private StockReserveCommand buildStockReserveCommand(ReservationDO reservation) {
        StockReserveCommand command = new StockReserveCommand();
        command.setReservationId(reservation.getReservationId());
        command.setRequestId(reservation.getRequestId());
        command.setIdempotencyKey(reservation.getIdempotencyKey());
        command.setUserId(reservation.getUserId());
        command.setActivityId(reservation.getActivityId());
        command.setTicketId(reservation.getTicketId());
        command.setQuantity(reservation.getQuantity());
        command.setStatus(reservation.getStatus());
        command.setExpireSeconds(reservationExpireSeconds);
        command.setExpireAtEpochSecond(reservation.getExpireAt().getEpochSecond());
        return command;
    }

    /**
     * 根据库存预扣结果映射业务异常。
     *
     * @param reserveResult 预扣结果
     * @return 业务异常
     */
    private BusinessException mapReserveException(StockReserveResult reserveResult) {
        if (SeckillConstants.RESERVE_RESULT_DUPLICATE.equals(reserveResult.getResultCode())) {
            return new BusinessException(ErrorCode.SECKILL_DUPLICATE_REQUEST);
        }
        if (SeckillConstants.RESERVE_RESULT_OUT_OF_STOCK.equals(reserveResult.getResultCode())) {
            return new BusinessException(ErrorCode.SECKILL_STOCK_NOT_ENOUGH);
        }
        return new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 构造下单请求事件。
     *
     * @param reservation 预扣记录
     * @return 下单请求事件
     */
    private OrderCreateRequestedEvent buildOrderCreateEvent(ReservationDO reservation) {
        OrderCreateRequestedEvent event = new OrderCreateRequestedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(OrderEventConstants.ORDER_CREATE_REQUESTED);
        event.setOccurredAt(reservation.getOccurredAt());
        event.setRequestId(reservation.getRequestId());
        event.setIdempotencyKey(reservation.getIdempotencyKey());
        event.setReservationId(reservation.getReservationId());
        event.setActivityId(reservation.getActivityId());
        event.setTicketId(reservation.getTicketId());
        event.setUserId(reservation.getUserId());
        event.setQuantity(reservation.getQuantity());
        event.setExpireAt(reservation.getExpireAt());
        event.setSource(OrderEventConstants.SOURCE_SECKILL_SERVICE);
        return event;
    }

    /**
     * 构造预扣响应。
     *
     * @param reservation 预扣记录
     * @return 预扣响应
     */
    private SeckillReserveResponse buildReserveResponse(ReservationDO reservation) {
        SeckillReserveResponse response = new SeckillReserveResponse();
        response.setReservationId(reservation.getReservationId());
        response.setStatus(reservation.getStatus());
        response.setExpireAt(reservation.getExpireAt());
        return response;
    }
}
