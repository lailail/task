package com.example.ticket.common.reliable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 可靠消息任务存储抽象。
 * 用于把模板层与 MyBatis-Plus Mapper 解耦，避免公共代码侵入具体持久化实现。
 *
 * @param <TTask> 任务类型
 */
public interface ReliableMessageTaskStore<TTask extends ReliableMessageTask> {
    /**
     * 插入新任务。
     *
     * @param task 新任务
     */
    void insert(TTask task);

    /**
     * 按主键更新任务。
     * 调用方通常只传入增量字段，因此实现方要保证只覆盖显式设置的字段。
     *
     * @param task 增量更新对象
     */
    void updateById(TTask task);

    /**
     * 加载到期任务。
     *
     * @param taskStatuses 允许参与补发的状态集合
     * @param currentTime 当前扫描时间
     * @param batchSize 单次扫描批量大小
     * @return 到期任务列表
     */
    List<TTask> loadDueTasks(List<String> taskStatuses, LocalDateTime currentTime, int batchSize);
}
