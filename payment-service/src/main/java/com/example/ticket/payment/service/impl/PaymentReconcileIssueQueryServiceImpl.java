package com.example.ticket.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.payment.domain.PaymentReconcileIssueDO;
import com.example.ticket.payment.mapper.PaymentReconcileIssueMapper;
import com.example.ticket.payment.request.PaymentReconcileIssueQueryRequest;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;
import com.example.ticket.payment.service.PaymentReconcileIssueQueryService;
import com.example.ticket.payment.support.PaymentReconcileIssueResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 支付对账异常查询服务实现。
 * 用于把异常事实表映射为内部查询响应，避免控制层直接操作持久化对象。
 */
@Service
public class PaymentReconcileIssueQueryServiceImpl implements PaymentReconcileIssueQueryService {
    private static final Logger log = LoggerFactory.getLogger(PaymentReconcileIssueQueryServiceImpl.class);

    private final PaymentReconcileIssueMapper paymentReconcileIssueMapper;

    /**
     * 构造支付对账异常查询服务。
     *
     * @param paymentReconcileIssueMapper 对账异常 Mapper
     */
    public PaymentReconcileIssueQueryServiceImpl(PaymentReconcileIssueMapper paymentReconcileIssueMapper) {
        this.paymentReconcileIssueMapper = paymentReconcileIssueMapper;
    }

    /**
     * 查询异常列表。
     *
     * @param request 查询条件
     * @return 异常列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentReconcileIssueResponse> queryIssues(PaymentReconcileIssueQueryRequest request) {
        LambdaQueryWrapper<PaymentReconcileIssueDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getPaymentRequestId())) {
            queryWrapper.eq(PaymentReconcileIssueDO::getPaymentRequestId, request.getPaymentRequestId());
        }
        if (request.getOrderId() != null) {
            queryWrapper.eq(PaymentReconcileIssueDO::getOrderId, request.getOrderId());
        }
        if (StringUtils.hasText(request.getIssueStatus())) {
            queryWrapper.eq(PaymentReconcileIssueDO::getIssueStatus, request.getIssueStatus());
        }
        queryWrapper.orderByDesc(PaymentReconcileIssueDO::getLastDetectedAt);
        List<PaymentReconcileIssueResponse> responses = paymentReconcileIssueMapper.selectList(queryWrapper).stream()
                .map(PaymentReconcileIssueResponseMapper::toResponse)
                .toList();
        log.info(
                "支付对账异常查询完成，paymentRequestId={}, orderId={}, issueStatus={}, count={}",
                request.getPaymentRequestId(),
                request.getOrderId(),
                request.getIssueStatus(),
                responses.size()
        );
        return responses;
    }

    /**
     * 查询异常详情。
     *
     * @param issueId 异常标识
     * @return 异常详情
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentReconcileIssueResponse queryIssueDetail(Long issueId) {
        PaymentReconcileIssueDO issue = paymentReconcileIssueMapper.selectById(issueId);
        if (issue == null) {
            log.warn("支付对账异常详情未命中，issueId={}", issueId);
            throw new BusinessException(ErrorCode.PAYMENT_RECONCILE_ISSUE_NOT_FOUND);
        }
        log.info("支付对账异常详情查询成功，issueId={}, paymentRequestId={}, orderId={}, issueStatus={}", issueId, issue.getPaymentRequestId(), issue.getOrderId(), issue.getIssueStatus());
        return PaymentReconcileIssueResponseMapper.toResponse(issue);
    }
}
