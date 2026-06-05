package com.example.ticket.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.payment.domain.PaymentResultTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付结果补偿任务 Mapper。
 * 用于承接 `payment_result_task` 表的基础读写能力，避免补偿查询散落到业务层。
 */
@Mapper
public interface PaymentResultTaskMapper extends BaseMapper<PaymentResultTaskDO> {
}
