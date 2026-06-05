package com.example.ticket.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.order.domain.OrderEventLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单事件日志表 Mapper。
 * 用于承接 `order_event_log` 表的基础 CRUD 查询能力。
 */
@Mapper
public interface OrderEventLogMapper extends BaseMapper<OrderEventLogDO> {
}
