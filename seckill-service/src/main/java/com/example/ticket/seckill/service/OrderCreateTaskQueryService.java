package com.example.ticket.seckill.service;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;

/**
 * 下单请求补偿任务查询服务。
 * 用于向后台治理页暴露抢票侧下单请求补偿任务的稳定分页查询能力。
 */
public interface OrderCreateTaskQueryService {

    /**
     * 分页查询下单请求补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    ReliableMessageTaskPageResponse queryOrderCreateTasks(ReliableMessageTaskQueryRequest request);
}
