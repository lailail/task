package com.example.ticket.seckill.gateway;

import com.example.ticket.seckill.gateway.impl.RedisStockReservationGateway;
import com.example.ticket.seckill.gateway.model.StockReserveCommand;
import com.example.ticket.seckill.gateway.model.StockReserveResult;
import com.example.ticket.seckill.support.SeckillConstants;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Redis 库存预扣网关单元测试。
 * 用于固定 Lua 预扣网关的 Key 约定、参数约定和结果映射语义。
 */
@ExtendWith(MockitoExtension.class)
class RedisStockReservationGatewayTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private DefaultRedisScript<List> reserveStockRedisScript;

    private RedisStockReservationGateway gateway;

    /**
     * 构造被测 Redis 网关。
     * 通过显式实例化方式固定当前测试只关注网关本身的参数编排。
     */
    @BeforeEach
    void setUp() {
        gateway = new RedisStockReservationGateway(stringRedisTemplate, reserveStockRedisScript);
    }

    /**
     * 预扣成功时，应按约定组装 Key 和参数，并返回成功结果。
     */
    @Test
    void should_build_expected_keys_and_map_success_result() {
        StockReserveCommand command = buildCommand();
        long expireAtEpochSecond = Instant.parse("2026-06-05T12:00:00Z").getEpochSecond();
        doReturn(List.of(SeckillConstants.RESERVE_RESULT_SUCCESS, String.valueOf(expireAtEpochSecond)))
                .when(stringRedisTemplate)
                .execute(any(RedisScript.class), anyList(), any(Object[].class));

        StockReserveResult result = gateway.reserve(command);

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(stringRedisTemplate).execute(any(RedisScript.class), keysCaptor.capture(), argsCaptor.capture());

        assertEquals(
                List.of(
                        "ticket:seckill:stock:1001:501",
                        "ticket:seckill:user-order:1001:10001",
                        "ticket:seckill:reservation:reservation-001",
                        "ticket:seckill:idempotent:idem-001"
                ),
                keysCaptor.getValue()
        );
        assertEquals("1", String.valueOf(argsCaptor.getValue()[0]));
        assertEquals("reservation-001", String.valueOf(argsCaptor.getValue()[1]));
        assertEquals("req-001", String.valueOf(argsCaptor.getValue()[8]));
        assertEquals(SeckillConstants.RESERVE_RESULT_SUCCESS, result.getResultCode());
        assertEquals("reservation-001", result.getReservationId());
        assertEquals(Instant.ofEpochSecond(expireAtEpochSecond), result.getExpireAt());
    }

    /**
     * 预扣失败时，应把 Lua 结果原样映射为失败结果对象。
     */
    @Test
    void should_map_failure_result_when_lua_returns_duplicate() {
        StockReserveCommand command = buildCommand();
        doReturn(List.of(SeckillConstants.RESERVE_RESULT_DUPLICATE, ""))
                .when(stringRedisTemplate)
                .execute(any(RedisScript.class), anyList(), any(Object[].class));

        StockReserveResult result = gateway.reserve(command);

        assertEquals(SeckillConstants.RESERVE_RESULT_DUPLICATE, result.getResultCode());
        assertNull(result.getReservationId());
        assertNull(result.getOccurredAt());
        assertNull(result.getExpireAt());
    }

    /**
     * 构造标准预扣命令。
     *
     * @return 预扣命令
     */
    private StockReserveCommand buildCommand() {
        StockReserveCommand command = new StockReserveCommand();
        command.setReservationId("reservation-001");
        command.setRequestId("req-001");
        command.setIdempotencyKey("idem-001");
        command.setUserId(10001L);
        command.setActivityId(1001L);
        command.setTicketId(501L);
        command.setQuantity(1);
        command.setStatus(SeckillConstants.RESERVATION_STATUS_RESERVED);
        command.setExpireSeconds(900L);
        command.setExpireAtEpochSecond(Instant.parse("2026-06-05T12:00:00Z").getEpochSecond());
        return command;
    }
}
