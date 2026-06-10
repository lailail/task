package com.example.ticket.job.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.job.request.ReservationRecordQueryRequest;
import com.example.ticket.job.response.ReservationRecordPageResponse;
import com.example.ticket.job.service.ReservationRecordQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预扣记录内部查询控制器。
 * 用于向后台治理页暴露稳定的预扣事实查询接口，不承载任何补偿或状态迁移逻辑。
 */
@Validated
@RestController
@RequestMapping("/api/v1/internal/reservation-records")
public class ReservationRecordInternalController {
    private static final Logger log = LoggerFactory.getLogger(ReservationRecordInternalController.class);

    private final ReservationRecordQueryService reservationRecordQueryService;

    /**
     * 构造预扣记录内部查询控制器。
     *
     * @param reservationRecordQueryService 预扣记录查询服务
     */
    public ReservationRecordInternalController(ReservationRecordQueryService reservationRecordQueryService) {
        this.reservationRecordQueryService = reservationRecordQueryService;
    }

    /**
     * 分页查询预扣记录。
     *
     * @param request 查询条件和分页参数
     * @return 分页后的预扣记录视图
     */
    @GetMapping
    public ApiResponse<ReservationRecordPageResponse> queryReservationRecords(ReservationRecordQueryRequest request) {
        log.info(
                "收到预扣记录分页查询请求，reservationId={}, requestId={}, userId={}, activityId={}, ticketId={}, reservationStatus={}, current={}, pageSize={}",
                request.getReservationId(),
                request.getRequestId(),
                request.getUserId(),
                request.getActivityId(),
                request.getTicketId(),
                request.getReservationStatus(),
                request.getCurrent(),
                request.getPageSize()
        );
        ReservationRecordPageResponse response = reservationRecordQueryService.queryReservationRecords(request);
        log.info(
                "预扣记录分页查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return ApiResponse.success(response);
    }
}
