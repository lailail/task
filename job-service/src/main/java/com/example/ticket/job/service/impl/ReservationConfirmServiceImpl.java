package com.example.ticket.job.service.impl;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.service.ReservationConfirmService;
import com.example.ticket.job.support.JobConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预扣确认服务实现。
 * 用于根据订单结果事件异步推进预扣记录状态，当前阶段先落订单创建成功后的确认动作。
 */
@Service
public class ReservationConfirmServiceImpl implements ReservationConfirmService {
    private final JobReservationRecordMapper reservationRecordMapper;

    /**
     * 构造预扣确认服务。
     *
     * @param reservationRecordMapper 预扣记录 Mapper
     */
    public ReservationConfirmServiceImpl(JobReservationRecordMapper reservationRecordMapper) {
        this.reservationRecordMapper = reservationRecordMapper;
    }

    /**
     * 处理下单结果事件。
     *
     * @param event 下单结果事件
     */
    @Override
    @Transactional
    public void handleOrderCreateResult(OrderCreateResultEvent event) {
        if (!JobConstants.ORDER_RESULT_TYPE_CREATED.equals(event.getEventType())) {
            return;
        }

        JobReservationRecordDO record = reservationRecordMapper.selectById(event.getReservationId());
        if (record == null) {
            return;
        }
        if (!JobConstants.RESERVATION_STATUS_RESERVED.equals(record.getReservationStatus())) {
            return;
        }

        record.setOrderId(event.getOrderId());
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_CONFIRMED);
        reservationRecordMapper.updateById(record);
    }
}
