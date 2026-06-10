package com.example.ticket.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.order.domain.OrderReservationRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单域预扣记录只读 Mapper。
 * 用于用户侧结果感知查询，读取预扣归属与当前预扣状态。
 */
@Mapper
public interface OrderReservationRecordMapper extends BaseMapper<OrderReservationRecordDO> {
}
