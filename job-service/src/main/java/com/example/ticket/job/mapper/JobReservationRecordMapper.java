package com.example.ticket.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.job.domain.JobReservationRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预扣记录表 Mapper。
 * 用于承接 `stock_reservation_record` 表在任务服务侧的基础查询能力。
 */
@Mapper
public interface JobReservationRecordMapper extends BaseMapper<JobReservationRecordDO> {
}
