package com.example.ticket.ticket.service.impl;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.ticket.dto.ActivityDTO;
import com.example.ticket.ticket.repository.TicketRepository;
import com.example.ticket.ticket.service.TicketQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 活动查询服务实现。
 * 当前阶段负责活动列表和详情查询的业务收口，后续替换存储层时不应影响控制器调用方式。
 */
@Service
public class TicketQueryServiceImpl implements TicketQueryService {
    private static final Logger log = LoggerFactory.getLogger(TicketQueryServiceImpl.class);

    private final TicketRepository ticketRepository;

    /**
     * 构造活动查询服务实现。
     *
     * @param ticketRepository 活动仓储
     */
    public TicketQueryServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * 查询当前可展示的活动列表。
     */
    @Override
    public List<ActivityDTO> listActivities() {
        List<ActivityDTO> activities = ticketRepository.findAllActivities();
        log.debug("活动查询服务返回活动列表，count={}", activities.size());
        return activities;
    }

    /**
     * 查询单个活动详情，不存在时返回稳定业务错误。
     */
    @Override
    public ActivityDTO getActivityDetail(Long activityId) {
        // 活动不存在时直接抛出稳定业务错误码，避免控制层自行拼装错误响应。
        return ticketRepository.findActivityById(activityId)
                .orElseThrow(() -> {
                    log.warn("活动详情查询未命中，activityId={}", activityId);
                    return new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
                });
    }
}
