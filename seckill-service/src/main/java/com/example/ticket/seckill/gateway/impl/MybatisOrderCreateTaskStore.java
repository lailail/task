package com.example.ticket.seckill.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.ReliableMessageTaskStore;
import com.example.ticket.seckill.domain.OrderCreateTaskDO;
import com.example.ticket.seckill.mapper.OrderCreateTaskMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 MyBatis-Plus 的抢票侧下单请求任务存储实现。
 * 用于把公共可靠消息模板需要的任务读写动作适配到 `order_create_task` 表。
 */
@Component
public class MybatisOrderCreateTaskStore implements ReliableMessageTaskStore<OrderCreateTaskDO> {
    private static final long FIRST_PAGE_NO = 1L;

    private final OrderCreateTaskMapper orderCreateTaskMapper;

    /**
     * 构造抢票侧下单请求任务存储实现。
     *
     * @param orderCreateTaskMapper 任务表 Mapper
     */
    public MybatisOrderCreateTaskStore(OrderCreateTaskMapper orderCreateTaskMapper) {
        this.orderCreateTaskMapper = orderCreateTaskMapper;
    }

    /**
     * 插入新任务。
     *
     * @param task 新任务
     */
    @Override
    public void insert(OrderCreateTaskDO task) {
        orderCreateTaskMapper.insert(task);
    }

    /**
     * 按主键更新任务。
     *
     * @param task 增量更新对象
     */
    @Override
    public void updateById(OrderCreateTaskDO task) {
        orderCreateTaskMapper.updateById(task);
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
    public List<OrderCreateTaskDO> loadDueTasks(List<String> taskStatuses, LocalDateTime currentTime, int batchSize) {
        LambdaQueryWrapper<OrderCreateTaskDO> queryWrapper = new LambdaQueryWrapper<OrderCreateTaskDO>()
                .in(OrderCreateTaskDO::getTaskStatus, taskStatuses)
                .le(OrderCreateTaskDO::getNextRetryAt, currentTime)
                .orderByAsc(OrderCreateTaskDO::getNextRetryAt);
        Page<OrderCreateTaskDO> page = new Page<>(FIRST_PAGE_NO, batchSize);
        return orderCreateTaskMapper.selectPage(page, queryWrapper).getRecords();
    }
}
