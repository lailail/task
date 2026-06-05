package com.example.ticket.seckill.controller;

import com.example.ticket.seckill.response.SeckillReserveResponse;
import com.example.ticket.seckill.service.SeckillService;
import com.example.ticket.seckill.support.SeckillConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 抢票控制器集成测试。
 * 用于固定抢票入口接口的最小请求与响应形态。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeckillControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeckillService seckillService;

    /**
     * 抢票预扣成功时，应返回统一成功响应。
     */
    @Test
    void should_reserve_stock_via_http() throws Exception {
        SeckillReserveResponse response = new SeckillReserveResponse();
        response.setReservationId("reservation-001");
        response.setStatus(SeckillConstants.RESERVATION_STATUS_RESERVED);
        response.setExpireAt(Instant.parse("2026-06-05T10:15:00Z"));

        when(seckillService.reserve(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/seckill/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "req-001",
                                  "idempotencyKey": "idem-001",
                                  "userId": 10001,
                                  "activityId": 1001,
                                  "ticketId": 501,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reservationId").value("reservation-001"))
                .andExpect(jsonPath("$.data.status").value(SeckillConstants.RESERVATION_STATUS_RESERVED));
    }
}
