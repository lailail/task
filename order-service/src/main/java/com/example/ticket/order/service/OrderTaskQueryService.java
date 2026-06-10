package com.example.ticket.order.service;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;

/**
 * 订单域补偿任务查询服务。
 * 用于向后台治理页暴露下单结果补偿任务和订单完成补偿任务的统一分页查询能力。
 */
public interface OrderTaskQueryService {

    /**
     * 分页查询下单结果补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    ReliableMessageTaskPageResponse queryOrderResultTasks(ReliableMessageTaskQueryRequest request);

    /**
     * 分页查询订单完成补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    ReliableMessageTaskPageResponse queryOrderCompleteTasks(ReliableMessageTaskQueryRequest request);
}
