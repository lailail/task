package com.example.ticket.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.order.domain.TicketOrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单表 Mapper。
 * 用于承接 `ticket_order` 表的基础 CRUD 查询能力。
 */
@Mapper
public interface TicketOrderMapper extends BaseMapper<TicketOrderDO> {
}
