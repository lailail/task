package com.example.ticket.seckill.controller;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.common.web.AuthenticatedUserHeaderSupport;
import jakarta.servlet.http.HttpServletRequest;
import com.example.ticket.seckill.request.SeckillReserveRequest;
import com.example.ticket.seckill.response.SeckillReserveResponse;
import com.example.ticket.seckill.service.SeckillService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 抢票入口控制器。
 * 当前只负责接收预扣请求并把调用转交给抢票服务。
 */
@RestController
@RequestMapping("/api/v1/seckill/reservations")
public class SeckillController {
    private static final Logger log = LoggerFactory.getLogger(SeckillController.class);

    private final SeckillService seckillService;

    /**
     * 构造抢票控制器。
     *
     * @param seckillService 抢票服务
     */
    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /**
     * 执行抢票预扣。
     *
     * @param httpServletRequest HTTP 请求
     * @param request 抢票预扣请求
     * @return 预扣响应
     */
    @PostMapping
    public ApiResponse<SeckillReserveResponse> reserve(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody SeckillReserveRequest request
    ) {
        AuthenticatedUser authenticatedUser = AuthenticatedUserHeaderSupport.extractAuthenticatedUser(httpServletRequest);
        log.info(
                "收到抢票预扣请求，requestId={}, userId={}, activityId={}, ticketId={}, quantity={}",
                request.getRequestId(),
                authenticatedUser.getUserId(),
                request.getActivityId(),
                request.getTicketId(),
                request.getQuantity()
        );
        SeckillReserveResponse response = seckillService.reserve(authenticatedUser, request);
        log.info(
                "抢票预扣接口处理完成，requestId={}, userId={}, activityId={}, ticketId={}, reservationId={}, status={}",
                request.getRequestId(),
                authenticatedUser.getUserId(),
                request.getActivityId(),
                request.getTicketId(),
                response.getReservationId(),
                response.getStatus()
        );
        return ApiResponse.success(response);
    }
}
