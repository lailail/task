package com.example.ticket.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQuerySupport;
import com.example.ticket.common.reliable.query.ReliableMessageTaskTypes;
import com.example.ticket.payment.domain.PaymentReconciledTaskDO;
import com.example.ticket.payment.domain.PaymentResultTaskDO;
import com.example.ticket.payment.mapper.PaymentReconciledTaskMapper;
import com.example.ticket.payment.mapper.PaymentResultTaskMapper;
import com.example.ticket.payment.service.PaymentTaskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 支付域补偿任务查询服务实现。
 * 用于收口支付域补偿任务的筛选、分页和统一响应映射逻辑。
 */
@Service
public class PaymentTaskQueryServiceImpl implements PaymentTaskQueryService {
    private static final Logger log = LoggerFactory.getLogger(PaymentTaskQueryServiceImpl.class);

    private final PaymentResultTaskMapper paymentResultTaskMapper;
    private final PaymentReconciledTaskMapper paymentReconciledTaskMapper;

    /**
     * 构造支付域补偿任务查询服务。
     *
     * @param paymentResultTaskMapper 支付结果补偿任务 Mapper
     * @param paymentReconciledTaskMapper 支付收敛补偿任务 Mapper
     */
    public PaymentTaskQueryServiceImpl(
            PaymentResultTaskMapper paymentResultTaskMapper,
            PaymentReconciledTaskMapper paymentReconciledTaskMapper
    ) {
        this.paymentResultTaskMapper = paymentResultTaskMapper;
        this.paymentReconciledTaskMapper = paymentReconciledTaskMapper;
    }

    /**
     * 分页查询支付结果补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public ReliableMessageTaskPageResponse queryPaymentResultTasks(ReliableMessageTaskQueryRequest request) {
        long current = ReliableMessageTaskQuerySupport.normalizeCurrent(request.getCurrent());
        long pageSize = ReliableMessageTaskQuerySupport.normalizePageSize(request.getPageSize());
        log.info(
                "开始查询支付结果补偿任务，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                current,
                pageSize
        );
        Page<PaymentResultTaskDO> page = paymentResultTaskMapper.selectPage(
                new Page<>(current, pageSize),
                buildPaymentResultTaskQueryWrapper(request)
        );
        ReliableMessageTaskPageResponse response = ReliableMessageTaskQuerySupport.toPageResponse(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                ReliableMessageTaskTypes.PAYMENT_RESULT
        );
        log.info(
                "支付结果补偿任务查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return response;
    }

    /**
     * 分页查询支付收敛补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public ReliableMessageTaskPageResponse queryPaymentReconciledTasks(ReliableMessageTaskQueryRequest request) {
        long current = ReliableMessageTaskQuerySupport.normalizeCurrent(request.getCurrent());
        long pageSize = ReliableMessageTaskQuerySupport.normalizePageSize(request.getPageSize());
        log.info(
                "开始查询支付收敛补偿任务，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                current,
                pageSize
        );
        Page<PaymentReconciledTaskDO> page = paymentReconciledTaskMapper.selectPage(
                new Page<>(current, pageSize),
                buildPaymentReconciledTaskQueryWrapper(request)
        );
        ReliableMessageTaskPageResponse response = ReliableMessageTaskQuerySupport.toPageResponse(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                ReliableMessageTaskTypes.PAYMENT_RECONCILED
        );
        log.info(
                "支付收敛补偿任务查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return response;
    }

    /**
     * 构造支付结果补偿任务查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<PaymentResultTaskDO> buildPaymentResultTaskQueryWrapper(ReliableMessageTaskQueryRequest request) {
        LambdaQueryWrapper<PaymentResultTaskDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskStatus())) {
            queryWrapper.eq(PaymentResultTaskDO::getTaskStatus, request.getTaskStatus().trim());
        }
        if (StringUtils.hasText(request.getBusinessKey())) {
            queryWrapper.eq(PaymentResultTaskDO::getBusinessKey, request.getBusinessKey().trim());
        }
        if (StringUtils.hasText(request.getEventKey())) {
            queryWrapper.eq(PaymentResultTaskDO::getEventKey, request.getEventKey().trim());
        }
        queryWrapper.orderByDesc(PaymentResultTaskDO::getNextRetryAt)
                .orderByDesc(PaymentResultTaskDO::getTaskId);
        return queryWrapper;
    }

    /**
     * 构造支付收敛补偿任务查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<PaymentReconciledTaskDO> buildPaymentReconciledTaskQueryWrapper(ReliableMessageTaskQueryRequest request) {
        LambdaQueryWrapper<PaymentReconciledTaskDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskStatus())) {
            queryWrapper.eq(PaymentReconciledTaskDO::getTaskStatus, request.getTaskStatus().trim());
        }
        if (StringUtils.hasText(request.getBusinessKey())) {
            queryWrapper.eq(PaymentReconciledTaskDO::getBusinessKey, request.getBusinessKey().trim());
        }
        if (StringUtils.hasText(request.getEventKey())) {
            queryWrapper.eq(PaymentReconciledTaskDO::getEventKey, request.getEventKey().trim());
        }
        queryWrapper.orderByDesc(PaymentReconciledTaskDO::getNextRetryAt)
                .orderByDesc(PaymentReconciledTaskDO::getTaskId);
        return queryWrapper;
    }
}
