package com.example.ticket.order.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.ReliableMessageTaskStore;
import com.example.ticket.order.domain.OrderResultTaskDO;
import com.example.ticket.order.mapper.OrderResultTaskMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 MyBatis-Plus 的下单结果任务存储实现。
 * 用于把公共可靠消息模板需要的任务读写动作适配到 `order_result_task` 表。
 */
@Component
public class MybatisOrderResultTaskStore implements ReliableMessageTaskStore<OrderResultTaskDO> {
    private static final long FIRST_PAGE_NO = 1L;

    private final OrderResultTaskMapper orderResultTaskMapper;

    /**
     * 构造下单结果任务存储实现。
     *
     * @param orderResultTaskMapper 任务表 Mapper
     */
    public MybatisOrderResultTaskStore(OrderResultTaskMapper orderResultTaskMapper) {
        this.orderResultTaskMapper = orderResultTaskMapper;
    }

    /**
     * 插入新任务。
     *
     * @param task 新任务
     */
    @Override
    public void insert(OrderResultTaskDO task) {
        orderResultTaskMapper.insert(task);
    }

    /**
     * 按主键更新任务。
     *
     * @param task 增量更新对象
     */
    @Override
    public void updateById(OrderResultTaskDO task) {
        orderResultTaskMapper.updateById(task);
    }

    /**
     * 加载到期任务。
     *
     * @param taskStatuses 允许参与补发的状态集合
     * @param currentTime 当前扫描时间
     * @param batchSize 单次扫描批量大小
     * @return 到期任务列表
     */
    @Override
    public List<OrderResultTaskDO> loadDueTasks(List<String> taskStatuses, LocalDateTime currentTime, int batchSize) {
        LambdaQueryWrapper<OrderResultTaskDO> queryWrapper = new LambdaQueryWrapper<OrderResultTaskDO>()
                .in(OrderResultTaskDO::getTaskStatus, taskStatuses)
                .le(OrderResultTaskDO::getNextRetryAt, currentTime)
                .orderByAsc(OrderResultTaskDO::getNextRetryAt);
        Page<OrderResultTaskDO> page = new Page<>(FIRST_PAGE_NO, batchSize);
        return orderResultTaskMapper.selectPage(page, queryWrapper).getRecords();
    }
}
