package com.example.ticket.order.service.impl;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.response.OrderStatusResponse;
import com.example.ticket.order.service.OrderQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单查询服务实现。
 * 用于向支付域返回最小订单状态事实，作为对账回查的内部依赖。
 */
@Service
public class OrderQueryServiceImpl implements OrderQueryService {
    private static final Logger log = LoggerFactory.getLogger(OrderQueryServiceImpl.class);

    private final TicketOrderMapper ticketOrderMapper;

    /**
     * 构造订单查询服务。
     *
     * @param ticketOrderMapper 订单 Mapper
     */
    public OrderQueryServiceImpl(TicketOrderMapper ticketOrderMapper) {
        this.ticketOrderMapper = ticketOrderMapper;
    }

    /**
     * 查询订单状态。
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
}
