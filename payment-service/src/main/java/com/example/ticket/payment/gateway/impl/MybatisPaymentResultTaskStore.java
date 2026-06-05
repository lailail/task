package com.example.ticket.payment.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.ReliableMessageTaskStore;
import com.example.ticket.payment.domain.PaymentResultTaskDO;
import com.example.ticket.payment.mapper.PaymentResultTaskMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 MyBatis-Plus 的支付结果任务存储实现。
 * 用于把公共可靠消息模板需要的任务读写动作适配到 `payment_result_task` 表。
 */
@Component
public class MybatisPaymentResultTaskStore implements ReliableMessageTaskStore<PaymentResultTaskDO> {
    private static final long FIRST_PAGE_NO = 1L;

    private final PaymentResultTaskMapper paymentResultTaskMapper;

    /**
     * 构造支付结果任务存储实现。
     *
     * @param paymentResultTaskMapper 支付结果任务 Mapper
     */
    public MybatisPaymentResultTaskStore(PaymentResultTaskMapper paymentResultTaskMapper) {
        this.paymentResultTaskMapper = paymentResultTaskMapper;
    }

    /**
     * 插入新的支付结果任务。
     *
     * @param task 新任务
     */
    @Override
    public void insert(PaymentResultTaskDO task) {
        paymentResultTaskMapper.insert(task);
    }

    /**
     * 按主键更新支付结果任务。
     *
     * @param task 增量更新对象
     */
    @Override
    public void updateById(PaymentResultTaskDO task) {
        paymentResultTaskMapper.updateById(task);
    }

    /**
     * 加载已到期的支付结果补发任务。
     *
     * @param taskStatuses 可补发状态集合
     * @param currentTime 当前扫描时间
     * @param batchSize 单次扫描批量大小
     * @return 到期任务列表
     */
    @Override
    public List<PaymentResultTaskDO> loadDueTasks(List<String> taskStatuses, LocalDateTime currentTime, int batchSize) {
        LambdaQueryWrapper<PaymentResultTaskDO> queryWrapper = new LambdaQueryWrapper<PaymentResultTaskDO>()
                .in(PaymentResultTaskDO::getTaskStatus, taskStatuses)
                .le(PaymentResultTaskDO::getNextRetryAt, currentTime)
                .orderByAsc(PaymentResultTaskDO::getNextRetryAt);
        Page<PaymentResultTaskDO> page = new Page<>(FIRST_PAGE_NO, batchSize);
        return paymentResultTaskMapper.selectPage(page, queryWrapper).getRecords();
    }
}
