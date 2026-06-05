package com.example.ticket.seckill.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticket.seckill.domain.SeckillActivityDO;
import com.example.ticket.seckill.domain.SeckillTicketItemDO;
import com.example.ticket.seckill.dto.SeckillActivityDTO;
import com.example.ticket.seckill.mapper.SeckillActivityMapper;
import com.example.ticket.seckill.mapper.SeckillTicketItemMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 抢票活动数据库仓储实现。
 * 当前通过 MyBatis-Plus 读取活动表和票种表，为抢票服务提供正式数据库事实来源。
 */
@Repository
public class DatabaseSeckillActivityRepository implements SeckillActivityRepository {
    private final SeckillActivityMapper activityMapper;
    private final SeckillTicketItemMapper ticketItemMapper;

    /**
     * 构造抢票活动数据库仓储。
     *
     * @param activityMapper 抢票活动表 Mapper
     * @param ticketItemMapper 抢票票种表 Mapper
     */
    public DatabaseSeckillActivityRepository(
            SeckillActivityMapper activityMapper,
            SeckillTicketItemMapper ticketItemMapper
    ) {
        this.activityMapper = activityMapper;
        this.ticketItemMapper = ticketItemMapper;
    }

    /**
     * 按活动和票种查询抢票活动。
     *
     * @param activityId 活动标识
     * @param ticketId 票种标识
     * @return 查询结果
     */
    @Override
    public Optional<SeckillActivityDTO> findByActivityIdAndTicketId(Long activityId, Long ticketId) {
        SeckillActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Optional.empty();
        }
        SeckillTicketItemDO ticketItem = ticketItemMapper.selectById(ticketId);
        if (ticketItem == null || !activityId.equals(ticketItem.getActivityId())) {
            return Optional.empty();
        }
        return Optional.of(toSeckillActivityDTO(activity, ticketItem));
    }

    /**
     * 查询当前阶段可用于预热和演示的全部抢票活动。
     *
     * @return 活动列表
     */
    @Override
    public List<SeckillActivityDTO> listActivities() {
        List<SeckillActivityDO> activities = activityMapper.selectList(new LambdaQueryWrapper<SeckillActivityDO>()
                .orderByAsc(SeckillActivityDO::getActivityId));
        if (activities.isEmpty()) {
            return List.of();
        }
        Map<Long, SeckillActivityDO> activityById = activities.stream()
                .collect(Collectors.toMap(SeckillActivityDO::getActivityId, Function.identity()));
        return ticketItemMapper.selectList(new LambdaQueryWrapper<SeckillTicketItemDO>()
                        .in(SeckillTicketItemDO::getActivityId, activityById.keySet())
                        .orderByAsc(SeckillTicketItemDO::getActivityId, SeckillTicketItemDO::getTicketId))
                .stream()
                .map(ticketItem -> toSeckillActivityDTO(activityById.get(ticketItem.getActivityId()), ticketItem))
                .toList();
    }

    /**
     * 把活动持久化对象和票种持久化对象转换成抢票活动 DTO。
     *
     * @param activity 活动持久化对象
     * @param ticketItem 票种持久化对象
     * @return 抢票活动 DTO
     */
    private SeckillActivityDTO toSeckillActivityDTO(SeckillActivityDO activity, SeckillTicketItemDO ticketItem) {
        SeckillActivityDTO dto = new SeckillActivityDTO();
        dto.setActivityId(activity.getActivityId());
        dto.setTicketId(ticketItem.getTicketId());
        dto.setActivityName(activity.getActivityName());
        dto.setSaleStatus(activity.getSaleStatus());
        dto.setAvailableStock(ticketItem.getAvailableStock());
        return dto;
    }
}
