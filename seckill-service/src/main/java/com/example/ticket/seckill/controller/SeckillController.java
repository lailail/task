package com.example.ticket.seckill.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.seckill.request.SeckillReserveRequest;
import com.example.ticket.seckill.response.SeckillReserveResponse;
import com.example.ticket.seckill.service.SeckillService;
import jakarta.validation.Valid;
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
     * @param request 抢票预扣请求
     * @return 预扣响应
     */
    @PostMapping
    public ApiResponse<SeckillReserveResponse> reserve(@Valid @RequestBody SeckillReserveRequest request) {
        return ApiResponse.success(seckillService.reserve(request));
    }
}
