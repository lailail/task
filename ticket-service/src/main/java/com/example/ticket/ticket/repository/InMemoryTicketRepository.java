package com.example.ticket.ticket.repository;

import com.example.ticket.ticket.dto.ActivityDTO;
import com.example.ticket.ticket.support.TicketSampleData;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 活动查询内存仓储实现。
 * 这是 Phase 2 的演示数据仓储，示例活动数据已集中到独立支持类中，避免硬编码散落在查询逻辑里。
 */
@Repository
@Profile("memory")
public class InMemoryTicketRepository implements TicketRepository {
    private final List<ActivityDTO> activities = TicketSampleData.buildActivities();

    /**
     * 查询全部演示活动。
     *
     * @return 演示活动列表
     */
    @Override
    public List<ActivityDTO> findAllActivities() {
        return activities;
    }

    /**
     * 按活动标识查询演示活动详情。
     *
     * @param activityId 活动标识
     * @return 查询结果
     */
    @Override
    public Optional<ActivityDTO> findActivityById(Long activityId) {
        return activities.stream()
                .filter(activity -> activity.getActivityId().equals(activityId))
                .findFirst();
    }
}
