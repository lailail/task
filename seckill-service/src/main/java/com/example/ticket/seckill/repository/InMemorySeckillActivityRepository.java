package com.example.ticket.seckill.repository;

import com.example.ticket.seckill.dto.SeckillActivityDTO;
import com.example.ticket.seckill.support.SeckillSampleData;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 抢票活动内存仓储实现。
 * 这是 Phase 3 的过渡实现，只用于让抢票主链路先具备本地可测能力。
 */
@Repository
@Profile("memory")
public class InMemorySeckillActivityRepository implements SeckillActivityRepository {
    private final List<SeckillActivityDTO> activities = SeckillSampleData.buildActivities();

    /**
     * 按活动和票种查询演示活动。
     *
     * @param activityId 活动标识
     * @param ticketId 票种标识
     * @return 查询结果
     */
    @Override
    public Optional<SeckillActivityDTO> findByActivityIdAndTicketId(Long activityId, Long ticketId) {
        return activities.stream()
                .filter(activity -> activity.getActivityId().equals(activityId) && activity.getTicketId().equals(ticketId))
                .findFirst();
    }

    /**
     * 查询全部演示活动。
     *
     * @return 活动列表
     */
    @Override
    public List<SeckillActivityDTO> listActivities() {
        return List.copyOf(activities);
    }
}
