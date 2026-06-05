package com.example.ticket.seckill.repository;

import com.example.ticket.seckill.domain.ReservationDO;
import com.example.ticket.seckill.domain.ReservationRecordDO;
import com.example.ticket.seckill.mapper.ReservationRecordMapper;
import com.example.ticket.seckill.support.SeckillConstants;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 预扣记录数据库仓储实现。
 * 当前通过 MyBatis-Plus 把 Redis 预扣成功后的正式业务事实写入 `stock_reservation_record` 表。
 */
@Repository
public class DatabaseReservationRecordRepository implements ReservationRecordRepository {
    private final ReservationRecordMapper reservationRecordMapper;

    /**
     * 构造预扣记录数据库仓储。
     *
     * @param reservationRecordMapper 预扣记录表 Mapper
     */
    public DatabaseReservationRecordRepository(ReservationRecordMapper reservationRecordMapper) {
        this.reservationRecordMapper = reservationRecordMapper;
    }

    /**
     * 保存预扣记录。
     *
     * @param reservation 预扣记录领域对象
     */
    @Override
    public void save(ReservationDO reservation) {
        reservationRecordMapper.insert(toReservationRecordDO(reservation));
    }

    /**
     * 把预扣领域对象转换成持久化对象。
     *
     * @param reservation 预扣领域对象
     * @return 预扣持久化对象
     */
    private ReservationRecordDO toReservationRecordDO(ReservationDO reservation) {
        ReservationRecordDO record = new ReservationRecordDO();
        record.setReservationId(reservation.getReservationId());
        record.setRequestId(reservation.getRequestId());
        record.setIdempotencyKey(reservation.getIdempotencyKey());
        record.setUserId(reservation.getUserId());
        record.setActivityId(reservation.getActivityId());
        record.setTicketId(reservation.getTicketId());
        record.setQuantity(reservation.getQuantity());
        record.setReservationStatus(reservation.getStatus());
        record.setSource(SeckillConstants.RESERVATION_SOURCE_SECKILL_SERVICE);
        record.setExpireAt(LocalDateTime.ofInstant(reservation.getExpireAt(), ZoneId.systemDefault()));
        return record;
    }
}
