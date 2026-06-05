package com.example.ticket.seckill.support;

import com.example.ticket.seckill.dto.SeckillActivityDTO;
import com.example.ticket.seckill.repository.SeckillActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 抢票库存预热器单元测试。
 * 用于固定本地演示库存写入 Redis 的边界，避免启动时把已有库存覆盖掉。
 */
@ExtendWith(MockitoExtension.class)
class SeckillStockWarmUpRunnerTest {

    @Mock
    private SeckillActivityRepository activityRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private SeckillStockWarmUpRunner stockWarmUpRunner;

    /**
     * 构造被测预热器。
     * 当前测试只关注启动预热逻辑，不依赖完整 Spring 上下文。
     */
    @BeforeEach
    void setUp() {
        stockWarmUpRunner = new SeckillStockWarmUpRunner(activityRepository, stringRedisTemplate);
    }

    /**
     * 启动预热时，应仅按活动清单尝试写入缺失库存，不主动覆盖已有值。
     */
    @Test
    void should_warm_up_stock_keys_with_set_if_absent() throws Exception {
        SeckillActivityDTO concert = buildActivity(1001L, 501L, 800);
        SeckillActivityDTO drama = buildActivity(1002L, 503L, 120);
        when(activityRepository.listActivities()).thenReturn(List.of(concert, drama));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        stockWarmUpRunner.run(null);

        verify(valueOperations, times(1))
                .setIfAbsent(SeckillRedisKeySupport.buildStockKey(1001L, 501L), "800");
        verify(valueOperations, times(1))
                .setIfAbsent(SeckillRedisKeySupport.buildStockKey(1002L, 503L), "120");
    }

    /**
     * 构造演示活动对象。
     *
     * @param activityId 活动标识
     * @param ticketId 票种标识
     * @param availableStock 可售库存
     * @return 活动对象
     */
    private SeckillActivityDTO buildActivity(Long activityId, Long ticketId, Integer availableStock) {
        SeckillActivityDTO activity = new SeckillActivityDTO();
        activity.setActivityId(activityId);
        activity.setTicketId(ticketId);
        activity.setActivityName("演示活动");
        activity.setSaleStatus(SeckillConstants.SALE_STATUS_ON_SALE);
        activity.setAvailableStock(availableStock);
        return activity;
    }
}
