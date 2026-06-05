package com.example.ticket.ticket.repository;

import com.example.ticket.ticket.dto.ActivityDTO;

import java.util.List;
import java.util.Optional;

/**
 * 活动查询仓储接口。
 * 用于抽象活动数据的读取方式，隔离服务层与具体数据源。
 */
public interface TicketRepository {

    /**
     * 查询全部活动。
     *
     * @return 活动列表
     */
    List<ActivityDTO> findAllActivities();

    /**
     * 按活动标识查询活动详情。
     *
     * @param activityId 活动标识
     * @return 查询结果
     */
    Optional<ActivityDTO> findActivityById(Long activityId);
}
