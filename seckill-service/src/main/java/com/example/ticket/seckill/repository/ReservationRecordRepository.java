package com.example.ticket.seckill.repository;

import com.example.ticket.seckill.domain.ReservationDO;

/**
 * 预扣记录仓储接口。
 * 用于隔离抢票服务与正式预扣记录表之间的持久化细节，降低业务层与 MyBatis 的直接耦合。
 */
public interface ReservationRecordRepository {

    /**
     * 保存预扣记录。
     *
     * @param reservation 预扣记录领域对象
     */
    void save(ReservationDO reservation);
}
