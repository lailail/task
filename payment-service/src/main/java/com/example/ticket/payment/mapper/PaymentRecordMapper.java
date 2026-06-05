package com.example.ticket.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.payment.domain.PaymentRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录 Mapper。
 * 用于承接 `payment_record` 表的基础持久化能力。
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecordDO> {
}
