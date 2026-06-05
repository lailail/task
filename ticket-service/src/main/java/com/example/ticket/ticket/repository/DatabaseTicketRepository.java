package com.example.ticket.ticket.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticket.ticket.domain.ActivityDO;
import com.example.ticket.ticket.domain.TicketItemDO;
import com.example.ticket.ticket.dto.ActivityDTO;
import com.example.ticket.ticket.dto.TicketItemDTO;
import com.example.ticket.ticket.mapper.ActivityMapper;
import com.example.ticket.ticket.mapper.TicketItemMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 活动数据库仓储实现。
 * 当前通过 MyBatis-Plus 访问活动表和票种表，并在仓储层完成活动与票种聚合。
 */
@Repository
public class DatabaseTicketRepository implements TicketRepository {
    private final ActivityMapper activityMapper;
    private final TicketItemMapper ticketItemMapper;

    /**
     * 构造活动数据库仓储。
     *
     * @param activityMapper 活动表 Mapper
     * @param ticketItemMapper 票种表 Mapper
     */
    public DatabaseTicketRepository(ActivityMapper activityMapper, TicketItemMapper ticketItemMapper) {
        this.activityMapper = activityMapper;
        this.ticketItemMapper = ticketItemMapper;
    }

    /**
     * 查询全部活动。
     *
     * @return 活动列表
     */
    @Override
    public List<ActivityDTO> findAllActivities() {
        List<ActivityDO> activities = activityMapper.selectList(new LambdaQueryWrapper<ActivityDO>()
                .orderByAsc(ActivityDO::getActivityId));
        if (activities.isEmpty()) {
            return List.of();
        }
        Map<Long, List<TicketItemDO>> ticketItemsByActivityId = queryTicketItemsByActivityIds(
                activities.stream().map(ActivityDO::getActivityId).toList()
        );
        return activities.stream()
                .map(activity -> toActivityDTO(activity, ticketItemsByActivityId.getOrDefault(activity.getActivityId(), List.of())))
                .toList();
    }

    /**
     * 按活动标识查询活动详情。
     *
     * @param activityId 活动标识
     * @return 查询结果
     */
    @Override
    public Optional<ActivityDTO> findActivityById(Long activityId) {
        ActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Optional.empty();
        }
        List<TicketItemDO> ticketItems = ticketItemMapper.selectList(new LambdaQueryWrapper<TicketItemDO>()
                .eq(TicketItemDO::getActivityId, activityId)
                .orderByAsc(TicketItemDO::getTicketId));
        return Optional.of(toActivityDTO(activity, ticketItems));
    }

    /**
     * 按活动标识批量查询票种并按活动分组。
     *
     * @param activityIds 活动标识列表
     * @return 按活动分组后的票种集合
     */
    private Map<Long, List<TicketItemDO>> queryTicketItemsByActivityIds(List<Long> activityIds) {
        return ticketItemMapper.selectList(new LambdaQueryWrapper<TicketItemDO>()
                        .in(TicketItemDO::getActivityId, activityIds)
                        .orderByAsc(TicketItemDO::getActivityId, TicketItemDO::getTicketId))
                .stream()
                .collect(Collectors.groupingBy(TicketItemDO::getActivityId));
    }

    /**
     * 把活动持久化对象和票种持久化对象转换成活动 DTO。
     *
     * @param activity 活动持久化对象
     * @param ticketItems 票种持久化对象列表
     * @return 活动 DTO
     */
    private ActivityDTO toActivityDTO(ActivityDO activity, List<TicketItemDO> ticketItems) {
        ActivityDTO dto = new ActivityDTO();
        dto.setActivityId(activity.getActivityId());
        dto.setActivityName(activity.getActivityName());
        dto.setCity(activity.getCity());
        dto.setVenueName(activity.getVenueName());
        dto.setSaleStatus(activity.getSaleStatus());
        dto.setTicketItems(ticketItems.stream().map(this::toTicketItemDTO).toList());
        return dto;
    }

    /**
     * 把票种持久化对象转换成票种 DTO。
     *
     * @param ticketItem 票种持久化对象
     * @return 票种 DTO
     */
    private TicketItemDTO toTicketItemDTO(TicketItemDO ticketItem) {
        TicketItemDTO dto = new TicketItemDTO();
        dto.setTicketId(ticketItem.getTicketId());
        dto.setTicketName(ticketItem.getTicketName());
        dto.setPrice(ticketItem.getPrice());
        dto.setAvailableStock(ticketItem.getAvailableStock());
        return dto;
    }
}
