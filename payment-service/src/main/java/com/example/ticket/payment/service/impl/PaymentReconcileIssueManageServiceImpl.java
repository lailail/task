package com.example.ticket.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.payment.domain.PaymentRecordDO;
import com.example.ticket.payment.domain.PaymentReconcileIssueDO;
import com.example.ticket.payment.mapper.PaymentReconcileIssueMapper;
import com.example.ticket.payment.mapper.PaymentRecordMapper;
import com.example.ticket.payment.request.PaymentReconcileIssueHandleRequest;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;
import com.example.ticket.payment.service.PaymentProcessService;
import com.example.ticket.payment.service.PaymentReconcileIssueManageService;
import com.example.ticket.payment.support.PaymentConstants;
import com.example.ticket.payment.support.PaymentReconcileIssueResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 支付对账异常人工治理服务实现。
 * 用于把人工重试、人工忽略和人工解决收敛到统一服务层，避免控制层直接篡改异常事实或支付对账状态。
 */
@Service
public class PaymentReconcileIssueManageServiceImpl implements PaymentReconcileIssueManageService {
    private static final Logger log = LoggerFactory.getLogger(PaymentReconcileIssueManageServiceImpl.class);
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final PaymentReconcileIssueMapper paymentReconcileIssueMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final PaymentProcessService paymentProcessService;

    /**
     * 构造支付对账异常人工治理服务。
     *
     * @param paymentReconcileIssueMapper 支付对账异常 Mapper
     * @param paymentRecordMapper 支付记录 Mapper
     * @param paymentProcessService 支付处理服务
     */
    public PaymentReconcileIssueManageServiceImpl(
            PaymentReconcileIssueMapper paymentReconcileIssueMapper,
            PaymentRecordMapper paymentRecordMapper,
            PaymentProcessService paymentProcessService
    ) {
        this.paymentReconcileIssueMapper = paymentReconcileIssueMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.paymentProcessService = paymentProcessService;
    }

    /**
     * 对指定异常执行人工重试。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    @Override
    @Transactional
    public PaymentReconcileIssueResponse retryIssue(Long issueId, PaymentReconcileIssueHandleRequest request) {
        PaymentReconcileIssueDO issue = loadIssueOrThrow(issueId);
        PaymentRecordDO paymentRecord = loadPaymentRecordOrThrow(issue.getPaymentRequestId());
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE_ID);
        log.info(
                "人工重试支付对账异常，issueId={}, paymentRequestId={}, paymentId={}, operator={}, currentIssueStatus={}, currentReconcileStatus={}",
                issueId,
                issue.getPaymentRequestId(),
                paymentRecord.getPaymentId(),
                request.getOperator(),
                issue.getIssueStatus(),
                paymentRecord.getReconcileStatus()
        );

        PaymentReconcileIssueDO issueUpdate = new PaymentReconcileIssueDO();
        issueUpdate.setIssueId(issueId);
        issueUpdate.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_OPEN);
        issueUpdate.setResolvedAt(null);
        issueUpdate.setLastDetectedAt(now);
        fillManualAction(issueUpdate, PaymentConstants.RECONCILE_MANUAL_ACTION_RETRY, request, now);
        paymentReconcileIssueMapper.updateById(issueUpdate);

        PaymentRecordDO recordUpdate = new PaymentRecordDO();
        recordUpdate.setPaymentId(paymentRecord.getPaymentId());
        recordUpdate.setReconcileStatus(PaymentConstants.RECONCILE_STATUS_PENDING);
        recordUpdate.setLastReconcileAt(now);
        paymentRecordMapper.updateById(recordUpdate);

        paymentProcessService.reconcilePaymentRequest(issue.getPaymentRequestId(), now);
        log.info(
                "人工重试支付对账异常完成，issueId={}, paymentRequestId={}, operator={}, targetIssueStatus={}, targetReconcileStatus={}",
                issueId,
                issue.getPaymentRequestId(),
                request.getOperator(),
                PaymentConstants.RECONCILE_ISSUE_STATUS_OPEN,
                PaymentConstants.RECONCILE_STATUS_PENDING
        );
        return PaymentReconcileIssueResponseMapper.toResponse(loadIssueOrThrow(issueId));
    }

    /**
     * 对指定异常执行人工忽略。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    @Override
    @Transactional
    public PaymentReconcileIssueResponse ignoreIssue(Long issueId, PaymentReconcileIssueHandleRequest request) {
        PaymentReconcileIssueDO issue = loadIssueOrThrow(issueId);
        ensureIssueIsOpen(issue);
        PaymentRecordDO paymentRecord = loadPaymentRecordOrThrow(issue.getPaymentRequestId());
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE_ID);
        log.info(
                "人工忽略支付对账异常，issueId={}, paymentRequestId={}, paymentId={}, operator={}, currentIssueStatus={}, currentReconcileStatus={}",
                issueId,
                issue.getPaymentRequestId(),
                paymentRecord.getPaymentId(),
                request.getOperator(),
                issue.getIssueStatus(),
                paymentRecord.getReconcileStatus()
        );

        PaymentReconcileIssueDO issueUpdate = new PaymentReconcileIssueDO();
        issueUpdate.setIssueId(issueId);
        issueUpdate.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_IGNORED);
        issueUpdate.setLastDetectedAt(now);
        fillManualAction(issueUpdate, PaymentConstants.RECONCILE_MANUAL_ACTION_IGNORE, request, now);
        paymentReconcileIssueMapper.updateById(issueUpdate);

        markPaymentRecordAsDone(paymentRecord, now);
        log.info(
                "人工忽略支付对账异常完成，issueId={}, paymentRequestId={}, operator={}, targetIssueStatus={}, targetReconcileStatus={}",
                issueId,
                issue.getPaymentRequestId(),
                request.getOperator(),
                PaymentConstants.RECONCILE_ISSUE_STATUS_IGNORED,
                PaymentConstants.RECONCILE_STATUS_DONE
        );
        return PaymentReconcileIssueResponseMapper.toResponse(loadIssueOrThrow(issueId));
    }

    /**
     * 对指定异常执行人工解决。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    @Override
    @Transactional
    public PaymentReconcileIssueResponse resolveIssue(Long issueId, PaymentReconcileIssueHandleRequest request) {
        PaymentReconcileIssueDO issue = loadIssueOrThrow(issueId);
        ensureIssueIsOpen(issue);
        PaymentRecordDO paymentRecord = loadPaymentRecordOrThrow(issue.getPaymentRequestId());
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE_ID);
        log.info(
                "人工解决支付对账异常，issueId={}, paymentRequestId={}, paymentId={}, operator={}, currentIssueStatus={}, currentReconcileStatus={}",
                issueId,
                issue.getPaymentRequestId(),
                paymentRecord.getPaymentId(),
                request.getOperator(),
                issue.getIssueStatus(),
                paymentRecord.getReconcileStatus()
        );

        PaymentReconcileIssueDO issueUpdate = new PaymentReconcileIssueDO();
        issueUpdate.setIssueId(issueId);
        issueUpdate.setIssueStatus(PaymentConstants.RECONCILE_ISSUE_STATUS_RESOLVED);
        issueUpdate.setResolvedAt(now);
        issueUpdate.setLastDetectedAt(now);
        fillManualAction(issueUpdate, PaymentConstants.RECONCILE_MANUAL_ACTION_RESOLVE, request, now);
        paymentReconcileIssueMapper.updateById(issueUpdate);

        markPaymentRecordAsDone(paymentRecord, now);
        log.info(
                "人工解决支付对账异常完成，issueId={}, paymentRequestId={}, operator={}, targetIssueStatus={}, targetReconcileStatus={}",
                issueId,
                issue.getPaymentRequestId(),
                request.getOperator(),
                PaymentConstants.RECONCILE_ISSUE_STATUS_RESOLVED,
                PaymentConstants.RECONCILE_STATUS_DONE
        );
        return PaymentReconcileIssueResponseMapper.toResponse(loadIssueOrThrow(issueId));
    }

    /**
     * 按支付请求标识加载支付记录。
     *
     * @param paymentRequestId 支付请求标识
     * @return 支付记录
     */
    private PaymentRecordDO loadPaymentRecordOrThrow(String paymentRequestId) {
        PaymentRecordDO paymentRecord = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecordDO>()
                        .eq(PaymentRecordDO::getPaymentRequestId, paymentRequestId)
        );
        if (paymentRecord == null) {
            throw new BusinessException(ErrorCode.PAYMENT_RECORD_NOT_FOUND);
        }
        return paymentRecord;
    }

    /**
     * 按异常标识加载异常记录。
     *
     * @param issueId 异常标识
     * @return 异常记录
     */
    private PaymentReconcileIssueDO loadIssueOrThrow(Long issueId) {
        PaymentReconcileIssueDO issue = paymentReconcileIssueMapper.selectById(issueId);
        if (issue == null) {
            throw new BusinessException(ErrorCode.PAYMENT_RECONCILE_ISSUE_NOT_FOUND);
        }
        return issue;
    }

    /**
     * 确保当前异常仍处于待治理状态。
     *
     * @param issue 异常记录
     */
    private void ensureIssueIsOpen(PaymentReconcileIssueDO issue) {
        if (!PaymentConstants.RECONCILE_ISSUE_STATUS_OPEN.equals(issue.getIssueStatus())) {
            log.warn(
                    "支付对账异常人工治理被拒绝，issueId={}, paymentRequestId={}, currentIssueStatus={}",
                    issue.getIssueId(),
                    issue.getPaymentRequestId(),
                    issue.getIssueStatus()
            );
            throw new BusinessException(ErrorCode.PAYMENT_RECONCILE_ISSUE_STATUS_INVALID);
        }
    }

    /**
     * 回填最近一次人工治理信息。
     *
     * @param issueUpdate 待更新异常对象
     * @param action 人工动作
     * @param request 处置请求
     * @param now 当前时间
     */
    private void fillManualAction(
            PaymentReconcileIssueDO issueUpdate,
            String action,
            PaymentReconcileIssueHandleRequest request,
            LocalDateTime now
    ) {
        issueUpdate.setManualAction(action);
        issueUpdate.setManualOperator(request.getOperator());
        issueUpdate.setManualOperatedAt(now);
        if (StringUtils.hasText(request.getNote())) {
            issueUpdate.setManualNote(request.getNote());
        }
    }

    /**
     * 把支付记录显式收敛到无需继续扫描的状态。
     *
     * @param paymentRecord 支付记录
     * @param now 当前时间
     */
    private void markPaymentRecordAsDone(PaymentRecordDO paymentRecord, LocalDateTime now) {
        PaymentRecordDO recordUpdate = new PaymentRecordDO();
        recordUpdate.setPaymentId(paymentRecord.getPaymentId());
        recordUpdate.setReconcileStatus(PaymentConstants.RECONCILE_STATUS_DONE);
        recordUpdate.setLastReconcileAt(now);
        paymentRecordMapper.updateById(recordUpdate);
    }
}
