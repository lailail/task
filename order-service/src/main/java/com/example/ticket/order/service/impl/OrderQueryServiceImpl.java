package com.example.ticket.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.order.domain.OrderReservationRecordDO;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.mapper.OrderReservationRecordMapper;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.request.UserOrderQueryRequest;
import com.example.ticket.order.response.OrderStatusResponse;
import com.example.ticket.order.response.ReservationResultResponse;
import com.example.ticket.order.response.UserOrderPageResponse;
import com.example.ticket.order.response.UserOrderResponse;
import com.example.ticket.order.service.OrderQueryService;
import com.example.ticket.order.support.OrderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 订单查询服务实现。
 * 用于承接内部订单状态查询、用户侧订单分页和抢票结果感知，所有查询只返回当前边界需要的 DTO。
 */
@Service
public class OrderQueryServiceImpl implements OrderQueryService {
    private static final Logger log = LoggerFactory.getLogger(OrderQueryServiceImpl.class);

    private final TicketOrderMapper ticketOrderMapper;
    private final OrderReservationRecordMapper reservationRecordMapper;

    /**
     * 构造订单查询服务。
     *
     * @param ticketOrderMapper 订单 Mapper
     * @param reservationRecordMapper 预扣记录 Mapper
     */
    public OrderQueryServiceImpl(
            TicketOrderMapper ticketOrderMapper,
            OrderReservationRecordMapper reservationRecordMapper
    ) {
        this.ticketOrderMapper = ticketOrderMapper;
        this.reservationRecordMapper = reservationRecordMapper;
    }

    /**
     * 查询订单状态，供内部支付与对账链路读取最小订单事实。
     *
     * @param orderId 订单标识
     * @return 订单状态响应
     */
    @Override
    @Transactional(readOnly = true)
    public OrderStatusResponse queryOrderStatus(Long orderId) {
        TicketOrderDO order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单状态查询未命中，orderId={}", orderId);
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        OrderStatusResponse response = new OrderStatusResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderStatus(order.getOrderStatus());
        log.debug("订单状态查询返回结果，orderId={}, orderStatus={}", orderId, order.getOrderStatus());
        return response;
    }

    /**
     * 查询当前登录用户自己的订单分页。
     *
     * @param authenticatedUser 当前认证用户
     * @param request 分页查询请求
     * @return 只包含当前用户订单事实的分页响应
     */
    @Override
    @Transactional(readOnly = true)
    public UserOrderPageResponse queryCurrentUserOrders(
            AuthenticatedUser authenticatedUser,
            UserOrderQueryRequest request
    ) {
        long current = normalizeCurrent(request);
        long pageSize = normalizePageSize(request);
        LambdaQueryWrapper<TicketOrderDO> queryWrapper = new LambdaQueryWrapper<TicketOrderDO>()
                .eq(TicketOrderDO::getUserId, authenticatedUser.getUserId())
                .orderByDesc(TicketOrderDO::getOrderId);
        Page<TicketOrderDO> page = ticketOrderMapper.selectPage(new Page<>(current, pageSize), queryWrapper);

        UserOrderPageResponse response = new UserOrderPageResponse();
        response.setCurrent(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        response.setRecords(mapUserOrders(page.getRecords()));
        log.debug("用户订单分页查询完成，userId={}, current={}, pageSize={}, total={}",
                authenticatedUser.getUserId(), response.getCurrent(), response.getPageSize(), response.getTotal());
        return response;
    }

    /**
     * 查询当前登录用户某次抢票的用户侧结果。
     *
     * @param authenticatedUser 当前认证用户
     * @param reservationId 预扣标识
     * @return 用户侧抢票结果响应
     */
    @Override
    @Transactional(readOnly = true)
    public ReservationResultResponse queryReservationResult(AuthenticatedUser authenticatedUser, String reservationId) {
        OrderReservationRecordDO reservation = reservationRecordMapper.selectById(reservationId);
        if (reservation == null || !Objects.equals(reservation.getUserId(), authenticatedUser.getUserId())) {
            log.info("用户抢票结果查询未命中或无权访问，userId={}, reservationId={}",
                    authenticatedUser.getUserId(), reservationId);
            return buildNotFoundResult(reservationId);
        }

        TicketOrderDO order = ticketOrderMapper.selectOne(new LambdaQueryWrapper<TicketOrderDO>()
                .eq(TicketOrderDO::getReservationId, reservationId)
                .eq(TicketOrderDO::getUserId, authenticatedUser.getUserId()));
        if (order == null) {
            return buildReservedResult(reservation);
        }
        return buildOrderResult(order, reservation);
    }

    /**
     * 标准化页码，避免非法页码触发无意义查询。
     *
     * @param request 分页查询请求
     * @return 可用于数据库分页的页码
     */
    private long normalizeCurrent(UserOrderQueryRequest request) {
        if (request == null || request.getCurrent() == null || request.getCurrent() <= 0) {
            return OrderConstants.DEFAULT_USER_ORDER_PAGE_NO;
        }
        return request.getCurrent();
    }

    /**
     * 标准化分页大小，避免用户侧接口出现无界查询。
     *
     * @param request 分页查询请求
     * @return 可用于数据库分页的分页大小
     */
    private long normalizePageSize(UserOrderQueryRequest request) {
        if (request == null || request.getPageSize() == null || request.getPageSize() <= 0) {
            return OrderConstants.DEFAULT_USER_ORDER_PAGE_SIZE;
        }
        return Math.min(request.getPageSize(), OrderConstants.MAX_USER_ORDER_PAGE_SIZE);
    }

    /**
     * 将订单持久化对象映射为用户侧订单 DTO，避免直接暴露 DO。
     *
     * @param orders 订单持久化对象列表
     * @return 用户侧订单响应列表
     */
    private List<UserOrderResponse> mapUserOrders(List<TicketOrderDO> orders) {
        return orders.stream().map(this::mapUserOrder).toList();
    }

    /**
     * 将单条订单持久化对象映射为用户侧订单响应。
     *
     * @param order 订单持久化对象
     * @return 用户侧订单响应
     */
    private UserOrderResponse mapUserOrder(TicketOrderDO order) {
        UserOrderResponse response = new UserOrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setReservationId(order.getReservationId());
        response.setActivityId(order.getActivityId());
        response.setTicketId(order.getTicketId());
        response.setQuantity(order.getQuantity());
        response.setAmountCent(order.getAmountCent());
        response.setOrderStatus(order.getOrderStatus());
        response.setExpireAt(order.getExpireAt());
        response.setPaidAt(order.getPaidAt());
        response.setClosedAt(order.getClosedAt());
        return response;
    }

    /**
     * 构造未找到结果，统一隐藏不存在和不属于当前用户两类事实。
     *
     * @param reservationId 预扣标识
     * @return 未找到语义的结果响应
     */
    private ReservationResultResponse buildNotFoundResult(String reservationId) {
        ReservationResultResponse response = new ReservationResultResponse();
        response.setReservationId(reservationId);
        response.setResultStatus(OrderConstants.USER_RESULT_STATUS_NOT_FOUND);
        return response;
    }

    /**
     * 构造已预扣但订单尚未落库的结果，表达异步建单空窗。
     *
     * @param reservation 预扣记录
     * @return 预扣处理中语义的结果响应
     */
    private ReservationResultResponse buildReservedResult(OrderReservationRecordDO reservation) {
        ReservationResultResponse response = new ReservationResultResponse();
        response.setReservationId(reservation.getReservationId());
        response.setResultStatus(OrderConstants.USER_RESULT_STATUS_RESERVED);
        response.setReservationStatus(reservation.getReservationStatus());
        response.setActivityId(reservation.getActivityId());
        response.setTicketId(reservation.getTicketId());
        response.setQuantity(reservation.getQuantity());
        response.setExpireAt(reservation.getExpireAt());
        return response;
    }

    /**
     * 构造已有订单时的用户侧结果，优先返回订单事实。
     *
     * @param order 订单记录
     * @param reservation 预扣记录
     * @return 订单结果响应
     */
    private ReservationResultResponse buildOrderResult(TicketOrderDO order, OrderReservationRecordDO reservation) {
        ReservationResultResponse response = new ReservationResultResponse();
        response.setReservationId(order.getReservationId());
        response.setResultStatus(mapUserResultStatus(order.getOrderStatus()));
        response.setReservationStatus(reservation.getReservationStatus());
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderStatus(order.getOrderStatus());
        response.setActivityId(order.getActivityId());
        response.setTicketId(order.getTicketId());
        response.setQuantity(order.getQuantity());
        response.setAmountCent(order.getAmountCent());
        response.setExpireAt(order.getExpireAt());
        response.setPaidAt(order.getPaidAt());
        response.setClosedAt(order.getClosedAt());
        return response;
    }

    /**
     * 将订单内部状态映射为用户侧结果状态，避免前端理解内部状态机细节。
     *
     * @param orderStatus 订单内部状态
     * @return 用户侧结果状态
     */
    private String mapUserResultStatus(String orderStatus) {
        return switch (orderStatus) {
            case OrderConstants.ORDER_STATUS_PAID -> OrderConstants.USER_RESULT_STATUS_ORDER_PAID;
            case OrderConstants.ORDER_STATUS_COMPLETED -> OrderConstants.USER_RESULT_STATUS_ORDER_COMPLETED;
            case OrderConstants.ORDER_STATUS_CLOSED -> OrderConstants.USER_RESULT_STATUS_ORDER_CLOSED;
            case OrderConstants.ORDER_STATUS_CANCELLED -> OrderConstants.USER_RESULT_STATUS_ORDER_CANCELLED;
            default -> OrderConstants.USER_RESULT_STATUS_ORDER_CREATED;
        };
    }
}
