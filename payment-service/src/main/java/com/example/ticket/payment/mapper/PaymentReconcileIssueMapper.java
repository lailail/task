package com.example.ticket.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.payment.domain.PaymentReconcileIssueDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付对账异常 Mapper。
 * 用于承接 `payment_reconcile_issue` 表的基础读写能力。
 */
@Mapper
public interface PaymentReconcileIssueMapper extends BaseMapper<PaymentReconcileIssueDO> {
}
