package com.example.ticket.payment.service;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;

/**
 * 支付域补偿任务查询服务。
 * 用于向后台治理页暴露支付结果补偿任务和支付收敛补偿任务的统一分页查询能力。
 */
public interface PaymentTaskQueryService {

    /**
     * 分页查询支付结果补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    ReliableMessageTaskPageResponse queryPaymentResultTasks(ReliableMessageTaskQueryRequest request);

    /**
     * 分页查询支付收敛补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    ReliableMessageTaskPageResponse queryPaymentReconciledTasks(ReliableMessageTaskQueryRequest request);
}
