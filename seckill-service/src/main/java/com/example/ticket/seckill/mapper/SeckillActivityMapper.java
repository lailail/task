package com.example.ticket.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.seckill.domain.SeckillActivityDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抢票活动表 Mapper。
 * 用于承接 `ticket_activity` 表在抢票服务侧的基础查询能力。
 */
@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivityDO> {
}
