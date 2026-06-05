package com.example.ticket.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.seckill.domain.SeckillTicketItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抢票票种表 Mapper。
 * 用于承接 `ticket_item` 表在抢票服务侧的基础查询能力。
 */
@Mapper
public interface SeckillTicketItemMapper extends BaseMapper<SeckillTicketItemDO> {
}
