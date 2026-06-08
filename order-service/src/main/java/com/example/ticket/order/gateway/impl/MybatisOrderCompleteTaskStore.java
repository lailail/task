package com.example.ticket.order.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.ReliableMessageTaskStore;
import com.example.ticket.order.domain.OrderCompleteTaskDO;
import com.example.ticket.order.mapper.OrderCompleteTaskMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 MyBatis-Plus 的订单完成任务存储实现。
 * 用于把可靠消息模板适配到 `order_complete_task` 表。
 */
@Component
public class MybatisOrderCompleteTaskStore implements ReliableMessageTaskStore<OrderCompleteTaskDO> {
    private static final long FIRST_PAGE_NO = 1L;

    private final OrderCompleteTaskMapper orderCompleteTaskMapper;

    /**
     * 构造订单完成任务存储实现。
     *
     * @param orderCompleteTaskMapper 订单完成任务 Mapper
     */
    public MybatisOrderCompleteTaskStore(OrderCompleteTaskMapper orderCompleteTaskMapper) {
        this.orderCompleteTaskMapper = orderCompleteTaskMapper;
    }

    /**
     * 插入新任务。
     *
     * @param task 新任务
     */
    @Override
    public void insert(OrderCompleteTaskDO task) {
        orderCompleteTaskMapper.insert(task);
    }

    /**
     * 按主键更新任务。
     *
     * @param task 增量更新对象
     */
    @Override
    public void updateById(OrderCompleteTaskDO task) {
        orderCompleteTaskMapper.updateById(task);
    }

    /**
     * 加载到期任务。
     *
     * @param taskStatuses 允许补发的状态集合
     * @param currentTime 当前扫描时间
     * @param batchSize 批量大小
     * @return 到期任务列表
     */
    @Override
    public List<OrderCompleteTaskDO> loadDueTasks(List<String> taskStatuses, LocalDateTime currentTime, int batchSize) {
        LambdaQueryWrapper<OrderCompleteTaskDO> queryWrapper = new LambdaQueryWrapper<OrderCompleteTaskDO>()
                .in(OrderCompleteTaskDO::getTaskStatus, taskStatuses)
                .le(OrderCompleteTaskDO::getNextRetryAt, currentTime)
                .orderByAsc(OrderCompleteTaskDO::getNextRetryAt);
        Page<OrderCompleteTaskDO> page = new Page<>(FIRST_PAGE_NO, batchSize);
        return orderCompleteTaskMapper.selectPage(page, queryWrapper).getRecords();
    }
}
