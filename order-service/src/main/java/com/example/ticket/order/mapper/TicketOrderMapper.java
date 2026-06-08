package com.example.ticket.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.order.domain.TicketOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 订单表 Mapper。
 * 用于承接 `ticket_order` 表的基础 CRUD 查询能力。
 */
@Mapper
public interface TicketOrderMapper extends BaseMapper<TicketOrderDO> {

    /**
     * 仅在订单仍处于待支付时推进到已支付。
     *
     * @param orderId 订单标识
     * @param paidAt 支付完成时间
     * @return 成功更新的记录数
     */
    @Update("""
            UPDATE ticket_order
            SET order_status = 'PAID',
                paid_at = #{paidAt}
            WHERE order_id = #{orderId}
              AND order_status = 'CREATED'
            """)
    int markPaidIfCreated(@Param("orderId") Long orderId, @Param("paidAt") LocalDateTime paidAt);

    /**
     * 仅在订单仍处于待支付时推进到已取消。
     *
     * @param orderId 订单标识
     * @param closedAt 关闭时间
     * @return 成功更新的记录数
     */
    @Update("""
            UPDATE ticket_order
            SET order_status = 'CANCELLED',
                closed_at = #{closedAt}
            WHERE order_id = #{orderId}
              AND order_status = 'CREATED'
            """)
    int markCancelledIfCreated(@Param("orderId") Long orderId, @Param("closedAt") LocalDateTime closedAt);

    /**
     * 仅在订单仍处于已支付时推进到已完成。
     *
     * @param orderId 订单标识
     * @return 成功更新的记录数
     */
    @Update("""
            UPDATE ticket_order
            SET order_status = 'COMPLETED'
            WHERE order_id = #{orderId}
              AND order_status = 'PAID'
            """)
    int markCompletedIfPaid(@Param("orderId") Long orderId);
}
