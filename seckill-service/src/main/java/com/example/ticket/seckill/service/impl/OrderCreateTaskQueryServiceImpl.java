package com.example.ticket.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQuerySupport;
import com.example.ticket.common.reliable.query.ReliableMessageTaskTypes;
import com.example.ticket.seckill.domain.OrderCreateTaskDO;
import com.example.ticket.seckill.mapper.OrderCreateTaskMapper;
import com.example.ticket.seckill.service.OrderCreateTaskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 下单请求补偿任务查询服务实现。
 * 用于收口抢票侧补偿任务查询逻辑，统一处理筛选条件、分页边界和响应映射。
 */
@Service
public class OrderCreateTaskQueryServiceImpl implements OrderCreateTaskQueryService {
    private static final Logger log = LoggerFactory.getLogger(OrderCreateTaskQueryServiceImpl.class);

    private final OrderCreateTaskMapper orderCreateTaskMapper;

    /**
     * 构造下单请求补偿任务查询服务。
     *
     * @param orderCreateTaskMapper 下单请求补偿任务 Mapper
     */
    public OrderCreateTaskQueryServiceImpl(OrderCreateTaskMapper orderCreateTaskMapper) {
        this.orderCreateTaskMapper = orderCreateTaskMapper;
    }

    /**
     * 分页查询下单请求补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public ReliableMessageTaskPageResponse queryOrderCreateTasks(ReliableMessageTaskQueryRequest request) {
        long current = ReliableMessageTaskQuerySupport.normalizeCurrent(request.getCurrent());
        long pageSize = ReliableMessageTaskQuerySupport.normalizePageSize(request.getPageSize());

        log.info(
                "开始查询下单请求补偿任务，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                current,
                pageSize
        );

        Page<OrderCreateTaskDO> page = orderCreateTaskMapper.selectPage(
                new Page<>(current, pageSize),
                buildQueryWrapper(request)
        );

        ReliableMessageTaskPageResponse response = ReliableMessageTaskQuerySupport.toPageResponse(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                ReliableMessageTaskTypes.ORDER_CREATE
        );
        log.info(
                "下单请求补偿任务查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return response;
    }

    /**
     * 构造下单请求补偿任务查询条件。
     * 这里只拼装显式传入的过滤项，避免空条件被误拼成无效查询。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<OrderCreateTaskDO> buildQueryWrapper(ReliableMessageTaskQueryRequest request) {
        LambdaQueryWrapper<OrderCreateTaskDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskStatus())) {
            queryWrapper.eq(OrderCreateTaskDO::getTaskStatus, request.getTaskStatus().trim());
        }
        if (StringUtils.hasText(request.getBusinessKey())) {
            queryWrapper.eq(OrderCreateTaskDO::getBusinessKey, request.getBusinessKey().trim());
        }
        if (StringUtils.hasText(request.getEventKey())) {
            queryWrapper.eq(OrderCreateTaskDO::getEventKey, request.getEventKey().trim());
        }
        queryWrapper.orderByDesc(OrderCreateTaskDO::getNextRetryAt)
                .orderByDesc(OrderCreateTaskDO::getTaskId);
        return queryWrapper;
    }
}
