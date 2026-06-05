package com.example.ticket.ticket.service;

import com.example.ticket.ticket.dto.ActivityDTO;

import java.util.List;

/**
 * 活动查询服务接口。
 * 定义活动列表和活动详情查询的对外能力。
 */
public interface TicketQueryService {

    /**
     * 查询活动列表。
     *
     * @return 活动列表
     */
    List<ActivityDTO> listActivities();

    /**
     * 查询活动详情。
     *
     * @param activityId 活动标识
     * @return 活动详情
     */
    ActivityDTO getActivityDetail(Long activityId);
}
