package com.example.ticket.payment.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.ReliableMessageTaskStore;
import com.example.ticket.payment.domain.PaymentReconciledTaskDO;
import com.example.ticket.payment.mapper.PaymentReconciledTaskMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 MyBatis-Plus 的支付收敛任务存储实现。
 * 用于把公共可靠消息模板适配到 `payment_reconciled_task` 表。
 */
@Component
public class MybatisPaymentReconciledTaskStore implements ReliableMessageTaskStore<PaymentReconciledTaskDO> {
    private static final long FIRST_PAGE_NO = 1L;

    private final PaymentReconciledTaskMapper paymentReconciledTaskMapper;

    /**
     * 构造支付收敛任务存储实现。
     *
     * @param paymentReconciledTaskMapper 支付收敛任务 Mapper
     */
    public MybatisPaymentReconciledTaskStore(PaymentReconciledTaskMapper paymentReconciledTaskMapper) {
        this.paymentReconciledTaskMapper = paymentReconciledTaskMapper;
    }

    /** 插入任务。 */
    @Override public void insert(PaymentReconciledTaskDO task) { paymentReconciledTaskMapper.insert(task); }
    /** 按主键更新任务。 */
    @Override public void updateById(PaymentReconciledTaskDO task) { paymentReconciledTaskMapper.updateById(task); }

    /**
     * 加载到期任务。
     *
     * @param taskStatuses 可补发状态
     * @param currentTime 当前时间
     * @param batchSize 批量大小
     * @return 到期任务列表
     */
    @Override
    public List<PaymentReconciledTaskDO> loadDueTasks(List<String> taskStatuses, LocalDateTime currentTime, int batchSize) {
        LambdaQueryWrapper<PaymentReconciledTaskDO> queryWrapper = new LambdaQueryWrapper<PaymentReconciledTaskDO>()
                .in(PaymentReconciledTaskDO::getTaskStatus, taskStatuses)
                .le(PaymentReconciledTaskDO::getNextRetryAt, currentTime)
                .orderByAsc(PaymentReconciledTaskDO::getNextRetryAt);
        return paymentReconciledTaskMapper.selectPage(new Page<>(FIRST_PAGE_NO, batchSize), queryWrapper).getRecords();
    }
}
