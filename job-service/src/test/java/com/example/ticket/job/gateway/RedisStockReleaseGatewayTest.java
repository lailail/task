package com.example.ticket.job.gateway;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.gateway.impl.RedisStockReleaseGateway;
import com.example.ticket.job.support.JobConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Redis 库存回补网关单元测试。
 * 用于固定 Lua 回补网关的 Key 约定、参数约定和结果映射语义。
 */
@ExtendWith(MockitoExtension.class)
class RedisStockReleaseGatewayTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private DefaultRedisScript<List> releaseStockRedisScript;

    private RedisStockReleaseGateway gateway;

    /**
     * 构造被测 Redis 回补网关。
     * 当前测试通过显式实例化方式固定测试只关注 Key 编排和脚本参数传递。
     */
    @BeforeEach
    void setUp() {
        gateway = new RedisStockReleaseGateway(stringRedisTemplate, releaseStockRedisScript, 86400L);
    }

    /**
     * 回补成功时，应按约定组装 Key 和参数，并返回成功结果。
     */
    @Test
    void should_build_expected_keys_and_map_success_result_when_release_stock() {
        StockReleaseEvent event = buildReleaseEvent();
        doReturn(List.of(JobConstants.STOCK_RELEASE_RESULT_SUCCESS))
                .when(stringRedisTemplate)
                .execute(any(RedisScript.class), anyList(), any(Object[].class));

        boolean released = gateway.release(event);

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(stringRedisTemplate).execute(any(RedisScript.class), keysCaptor.capture(), argsCaptor.capture());

        assertEquals(
                List.of(
                        "ticket:seckill:stock:1001:501",
                        "ticket:seckill:user-order:1001:10001",
                        "ticket:seckill:reservation:reservation-001",
                        "ticket:stock:release:reservation-001",
                        "ticket:seckill:idempotent:idem-001"
                ),
                keysCaptor.getValue()
        );
        assertEquals("1", String.valueOf(argsCaptor.getValue()[0]));
        assertEquals(JobConstants.RESERVATION_STATUS_RELEASED, String.valueOf(argsCaptor.getValue()[1]));
        assertEquals("DB_ERROR", String.valueOf(argsCaptor.getValue()[2]));
        assertEquals("86400", String.valueOf(argsCaptor.getValue()[3]));
        assertTrue(released);
    }

    /**
     * 构造库存释放事件。
     *
     * @return 库存释放事件
     */
    private StockReleaseEvent buildReleaseEvent() {
        StockReleaseEvent event = new StockReleaseEvent();
        event.setEventId("release-001");
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:30Z"));
        event.setRequestId("req-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setReason("DB_ERROR");
        event.setIdempotencyKey("idem-001");
        return event;
    }
}
