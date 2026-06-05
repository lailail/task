package com.example.ticket.seckill.service;

import com.example.ticket.seckill.request.SeckillReserveRequest;
import com.example.ticket.seckill.response.SeckillReserveResponse;

/**
 * 抢票服务接口。
 * 定义抢票预扣主链路对控制层暴露的能力。
 */
public interface SeckillService {

    /**
     * 执行抢票预扣。
     *
     * @param request 抢票预扣请求
     * @return 预扣结果
     */
    SeckillReserveResponse reserve(SeckillReserveRequest request);
}
