package com.example.ticket.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.ticket.domain.TicketItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 票种表 Mapper。
 * 用于承接 `ticket_item` 表的基础 CRUD 查询能力。
 */
@Mapper
public interface TicketItemMapper extends BaseMapper<TicketItemDO> {
}
