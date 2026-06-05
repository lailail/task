package com.example.ticket.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.seckill.domain.OrderCreateTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抢票侧下单请求补偿任务表 Mapper。
 * 用于承接 `order_create_task` 表的基础持久化操作。
 */
@Mapper
public interface OrderCreateTaskMapper extends BaseMapper<OrderCreateTaskDO> {
}
