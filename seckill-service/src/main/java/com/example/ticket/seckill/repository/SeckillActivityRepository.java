package com.example.ticket.seckill.repository;

import com.example.ticket.seckill.dto.SeckillActivityDTO;

import java.util.List;
import java.util.Optional;

/**
 * 抢票活动仓储接口。
 * 用于抽象抢票活动读取能力，隔离服务层与当前阶段的演示数据实现。
 */
public interface SeckillActivityRepository {

    /**
     * 按活动和票种查询抢票活动。
     *
     * @param activityId 活动标识
     * @param ticketId 票种标识
     * @return 查询结果
     */
    Optional<SeckillActivityDTO> findByActivityIdAndTicketId(Long activityId, Long ticketId);

    /**
     * 查询当前阶段可用于预热和演示的全部抢票活动。
     *
     * @return 活动列表
     */
    List<SeckillActivityDTO> listActivities();
}
