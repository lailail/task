package com.example.ticket.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.payment.domain.PaymentReconciledTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付收敛任务 Mapper。
 * 用于承接 `payment_reconciled_task` 表的基础读写能力。
 */
@Mapper
public interface PaymentReconciledTaskMapper extends BaseMapper<PaymentReconciledTaskDO> {
}
