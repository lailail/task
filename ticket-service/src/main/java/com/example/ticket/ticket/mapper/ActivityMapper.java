package com.example.ticket.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.ticket.domain.ActivityDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动表 Mapper。
 * 用于承接 `ticket_activity` 表的基础 CRUD 查询能力。
 */
@Mapper
public interface ActivityMapper extends BaseMapper<ActivityDO> {
}
