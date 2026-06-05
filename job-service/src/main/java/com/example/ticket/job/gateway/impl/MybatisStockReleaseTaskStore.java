package com.example.ticket.job.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.ReliableMessageTaskStore;
import com.example.ticket.job.domain.JobStockReleaseTaskDO;
import com.example.ticket.job.mapper.JobStockReleaseTaskMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 MyBatis-Plus 的库存释放任务存储实现。
 * 用于把公共可靠消息模板需要的任务读写动作适配到 `stock_release_task` 表。
 */
@Component
public class MybatisStockReleaseTaskStore implements ReliableMessageTaskStore<JobStockReleaseTaskDO> {
    private static final long FIRST_PAGE_NO = 1L;

    private final JobStockReleaseTaskMapper stockReleaseTaskMapper;

    /**
     * 构造库存释放任务存储实现。
     *
     * @param stockReleaseTaskMapper 任务表 Mapper
     */
    public MybatisStockReleaseTaskStore(JobStockReleaseTaskMapper stockReleaseTaskMapper) {
        this.stockReleaseTaskMapper = stockReleaseTaskMapper;
    }

    /**
     * 插入新任务。
     *
     * @param task 新任务
     */
    @Override
    public void insert(JobStockReleaseTaskDO task) {
        stockReleaseTaskMapper.insert(task);
    }

    /**
     * 按主键更新任务。
     *
     * @param task 增量更新对象
     */
    @Override
    public void updateById(JobStockReleaseTaskDO task) {
        stockReleaseTaskMapper.updateById(task);
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
    public List<JobStockReleaseTaskDO> loadDueTasks(List<String> taskStatuses, LocalDateTime currentTime, int batchSize) {
        LambdaQueryWrapper<JobStockReleaseTaskDO> queryWrapper = new LambdaQueryWrapper<JobStockReleaseTaskDO>()
                .in(JobStockReleaseTaskDO::getTaskStatus, taskStatuses)
                .le(JobStockReleaseTaskDO::getNextRetryAt, currentTime)
                .orderByAsc(JobStockReleaseTaskDO::getNextRetryAt);
        Page<JobStockReleaseTaskDO> page = new Page<>(FIRST_PAGE_NO, batchSize);
        return stockReleaseTaskMapper.selectPage(page, queryWrapper).getRecords();
    }
}
