package com.example.ticket.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.job.domain.JobStockReleaseTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存释放补偿任务表 Mapper。
 * 用于承接 `stock_release_task` 表的基础读写能力，避免把补偿任务查询散落到业务层。
 */
@Mapper
public interface JobStockReleaseTaskMapper extends BaseMapper<JobStockReleaseTaskDO> {
}
