package com.example.ticket.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.seckill.domain.ReservationRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预扣记录表 Mapper。
 * 用于承接 `stock_reservation_record` 表的基础持久化操作，避免服务层直接依赖 SQL 细节。
 */
@Mapper
public interface ReservationRecordMapper extends BaseMapper<ReservationRecordDO> {
}
