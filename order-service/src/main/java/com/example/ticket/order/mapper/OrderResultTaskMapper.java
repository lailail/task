package com.example.ticket.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.order.domain.OrderResultTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 下单结果补偿任务表 Mapper。
 * 用于承接 `order_result_task` 表的基础读写能力，避免把补偿任务查询散落到业务层。
 */
@Mapper
public interface OrderResultTaskMapper extends BaseMapper<OrderResultTaskDO> {
}
