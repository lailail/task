package com.example.ticket.common.reliable.query;

import com.example.ticket.common.reliable.ReliableMessageTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 补偿任务查询支持类。
 * 用于统一分页参数归一化和任务字段映射，避免多个服务重复编写相同样板代码。
 */
public final class ReliableMessageTaskQuerySupport {

    /**
     * 禁止实例化支持类。
     */
    private ReliableMessageTaskQuerySupport() {
    }

    /**
     * 归一化页码，避免非法页码导致查询语义漂移。
     *
     * @param current 原始页码
     * @return 合法页码
     */
    public static long normalizeCurrent(Long current) {
        if (current == null || current < ReliableMessageTaskPageConstants.DEFAULT_PAGE_CURRENT) {
            return ReliableMessageTaskPageConstants.DEFAULT_PAGE_CURRENT;
        }
        return current;
    }

    /**
     * 归一化分页大小，避免无界查询放大治理接口开销。
     *
     * @param pageSize 原始分页大小
     * @return 合法分页大小
     */
    public static long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return ReliableMessageTaskPageConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, ReliableMessageTaskPageConstants.MAX_PAGE_SIZE);
    }

    /**
     * 把统一任务视图映射为后台分页响应。
     *
     * @param records 当前页记录
     * @param total 总记录数
     * @param current 当前页码
     * @param pageSize 分页大小
     * @param taskType 任务域类型
     * @return 统一分页响应
     * @param <TTask> 任务实体类型
     */
    public static <TTask extends ReliableMessageTask> ReliableMessageTaskPageResponse toPageResponse(
            Iterable<TTask> records,
            long total,
            long current,
            long pageSize,
            String taskType
    ) {
        ReliableMessageTaskPageResponse response = new ReliableMessageTaskPageResponse();
        List<ReliableMessageTaskResponse> taskResponses = new ArrayList<>();
        for (TTask record : records) {
            taskResponses.add(toResponse(record, taskType));
        }
        response.setRecords(taskResponses);
        response.setTotal(total);
        response.setCurrent(current);
        response.setPageSize(pageSize);
        return response;
    }

    /**
     * 把补偿任务实体映射为统一列表项响应。
     *
     * @param task 任务实体
     * @param taskType 任务域类型
     * @return 统一列表项响应
     */
    public static ReliableMessageTaskResponse toResponse(ReliableMessageTask task, String taskType) {
        ReliableMessageTaskResponse response = new ReliableMessageTaskResponse();
        response.setTaskId(task.getTaskId());
        response.setTaskType(taskType);
        response.setEventKey(task.getEventKey());
        response.setEventType(task.getEventType());
        response.setBusinessKey(task.getBusinessKey());
        response.setTaskStatus(task.getTaskStatus());
        response.setRetryCount(task.getRetryCount());
        response.setMaxRetryCount(task.getMaxRetryCount());
        response.setNextRetryAt(task.getNextRetryAt());
        response.setLastSentAt(task.getLastSentAt());
        response.setLastErrorMessage(task.getLastErrorMessage());
        return response;
    }
}
