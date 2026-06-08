package com.example.ticket.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.order.domain.OrderCompleteTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单完成补偿任务 Mapper。
 * 用于承接 `order_complete_task` 表的基础读写能力。
 */
@Mapper
public interface OrderCompleteTaskMapper extends BaseMapper<OrderCompleteTaskDO> {
}
