package com.example.ticket.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.job.domain.JobTicketOrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务侧订单表 Mapper。
 * 用于承接 `ticket_order` 表在任务服务侧的基础查询能力。
 */
@Mapper
public interface JobTicketOrderMapper extends BaseMapper<JobTicketOrderDO> {
}
