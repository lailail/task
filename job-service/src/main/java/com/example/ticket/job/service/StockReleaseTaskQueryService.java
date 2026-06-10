package com.example.ticket.job.service;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;

/**
 * 库存释放补偿任务查询服务。
 * 用于向后台治理页暴露库存释放补偿任务的统一分页查询能力。
 */
public interface StockReleaseTaskQueryService {

    /**
     * 分页查询库存释放补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    ReliableMessageTaskPageResponse queryStockReleaseTasks(ReliableMessageTaskQueryRequest request);
}
