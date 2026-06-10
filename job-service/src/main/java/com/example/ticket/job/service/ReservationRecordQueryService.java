package com.example.ticket.job.service;

import com.example.ticket.job.request.ReservationRecordQueryRequest;
import com.example.ticket.job.response.ReservationRecordPageResponse;

/**
 * 预扣记录查询服务。
 * 用于承接后台治理页的分页查询入口，统一收口查询条件、分页规则和返回映射。
 */
public interface ReservationRecordQueryService {

    /**
     * 分页查询预扣记录。
     *
     * @param request 查询条件和分页参数
     * @return 分页后的预扣记录视图
     */
    ReservationRecordPageResponse queryReservationRecords(ReservationRecordQueryRequest request);
}
