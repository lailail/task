package com.example.ticket.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.payment.domain.PaymentRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 支付记录 Mapper。
 * 用于承接 `payment_record` 表的基础持久化能力。
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecordDO> {

    /**
     * 仅在当前对账状态匹配时推进状态。
     *
     * @param paymentId 支付标识
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @param lastReconcileAt 最近一次对账时间
     * @return 成功更新的记录数
     */
    @Update("""
            UPDATE payment_record
            SET reconcile_status = #{targetStatus},
                last_reconcile_at = #{lastReconcileAt}
            WHERE payment_id = #{paymentId}
              AND reconcile_status = #{currentStatus}
            """)
    int updateReconcileStatusIfCurrent(
            @Param("paymentId") Long paymentId,
            @Param("currentStatus") String currentStatus,
            @Param("targetStatus") String targetStatus,
            @Param("lastReconcileAt") LocalDateTime lastReconcileAt
    );
}
