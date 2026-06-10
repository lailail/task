package com.example.ticket.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQuerySupport;
import com.example.ticket.common.reliable.query.ReliableMessageTaskTypes;
import com.example.ticket.order.domain.OrderCompleteTaskDO;
import com.example.ticket.order.domain.OrderResultTaskDO;
import com.example.ticket.order.mapper.OrderCompleteTaskMapper;
import com.example.ticket.order.mapper.OrderResultTaskMapper;
import com.example.ticket.order.service.OrderTaskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 订单域补偿任务查询服务实现。
 * 用于收口订单域可靠消息补偿任务的筛选、分页和统一响应映射逻辑。
 */
@Service
public class OrderTaskQueryServiceImpl implements OrderTaskQueryService {
    private static final Logger log = LoggerFactory.getLogger(OrderTaskQueryServiceImpl.class);

    private final OrderResultTaskMapper orderResultTaskMapper;
    private final OrderCompleteTaskMapper orderCompleteTaskMapper;

    /**
     * 构造订单域补偿任务查询服务。
     *
     * @param orderResultTaskMapper 下单结果补偿任务 Mapper
     * @param orderCompleteTaskMapper 订单完成补偿任务 Mapper
     */
    public OrderTaskQueryServiceImpl(
            OrderResultTaskMapper orderResultTaskMapper,
            OrderCompleteTaskMapper orderCompleteTaskMapper
    ) {
        this.orderResultTaskMapper = orderResultTaskMapper;
        this.orderCompleteTaskMapper = orderCompleteTaskMapper;
    }

    /**
     * 分页查询下单结果补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public ReliableMessageTaskPageResponse queryOrderResultTasks(ReliableMessageTaskQueryRequest request) {
        long current = ReliableMessageTaskQuerySupport.normalizeCurrent(request.getCurrent());
        long pageSize = ReliableMessageTaskQuerySupport.normalizePageSize(request.getPageSize());
        log.info(
                "开始查询下单结果补偿任务，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                current,
                pageSize
        );
        Page<OrderResultTaskDO> page = orderResultTaskMapper.selectPage(
                new Page<>(current, pageSize),
                buildOrderResultTaskQueryWrapper(request)
        );
        ReliableMessageTaskPageResponse response = ReliableMessageTaskQuerySupport.toPageResponse(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                ReliableMessageTaskTypes.ORDER_RESULT
        );
        log.info(
                "下单结果补偿任务查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return response;
    }

    /**
     * 分页查询订单完成补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public ReliableMessageTaskPageResponse queryOrderCompleteTasks(ReliableMessageTaskQueryRequest request) {
        long current = ReliableMessageTaskQuerySupport.normalizeCurrent(request.getCurrent());
        long pageSize = ReliableMessageTaskQuerySupport.normalizePageSize(request.getPageSize());
        log.info(
                "开始查询订单完成补偿任务，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                current,
                pageSize
        );
        Page<OrderCompleteTaskDO> page = orderCompleteTaskMapper.selectPage(
                new Page<>(current, pageSize),
                buildOrderCompleteTaskQueryWrapper(request)
        );
        ReliableMessageTaskPageResponse response = ReliableMessageTaskQuerySupport.toPageResponse(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                ReliableMessageTaskTypes.ORDER_COMPLETE
        );
        log.info(
                "订单完成补偿任务查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return response;
    }

    /**
     * 构造下单结果补偿任务查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<OrderResultTaskDO> buildOrderResultTaskQueryWrapper(ReliableMessageTaskQueryRequest request) {
        LambdaQueryWrapper<OrderResultTaskDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskStatus())) {
            queryWrapper.eq(OrderResultTaskDO::getTaskStatus, request.getTaskStatus().trim());
        }
        if (StringUtils.hasText(request.getBusinessKey())) {
            queryWrapper.eq(OrderResultTaskDO::getBusinessKey, request.getBusinessKey().trim());
        }
        if (StringUtils.hasText(request.getEventKey())) {
            queryWrapper.eq(OrderResultTaskDO::getEventKey, request.getEventKey().trim());
        }
        queryWrapper.orderByDesc(OrderResultTaskDO::getNextRetryAt)
                .orderByDesc(OrderResultTaskDO::getTaskId);
        return queryWrapper;
    }

    /**
     * 构造订单完成补偿任务查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<OrderCompleteTaskDO> buildOrderCompleteTaskQueryWrapper(ReliableMessageTaskQueryRequest request) {
        LambdaQueryWrapper<OrderCompleteTaskDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskStatus())) {
            queryWrapper.eq(OrderCompleteTaskDO::getTaskStatus, request.getTaskStatus().trim());
        }
        if (StringUtils.hasText(request.getBusinessKey())) {
            queryWrapper.eq(OrderCompleteTaskDO::getBusinessKey, request.getBusinessKey().trim());
        }
        if (StringUtils.hasText(request.getEventKey())) {
            queryWrapper.eq(OrderCompleteTaskDO::getEventKey, request.getEventKey().trim());
        }
        queryWrapper.orderByDesc(OrderCompleteTaskDO::getNextRetryAt)
                .orderByDesc(OrderCompleteTaskDO::getTaskId);
        return queryWrapper;
    }
}
