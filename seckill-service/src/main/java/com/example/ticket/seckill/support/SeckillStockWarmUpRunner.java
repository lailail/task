package com.example.ticket.seckill.support;

import com.example.ticket.seckill.dto.SeckillActivityDTO;
import com.example.ticket.seckill.repository.SeckillActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 抢票库存预热器。
 * 用于在本地演示阶段把过渡活动数据按“只在缺失时写入”的方式预热到 Redis 库存 Key。
 */
@Component
@ConditionalOnProperty(prefix = "ticket.seckill", name = "stock-warm-up-enabled", havingValue = "true", matchIfMissing = true)
public class SeckillStockWarmUpRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SeckillStockWarmUpRunner.class);

    private final SeckillActivityRepository activityRepository;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造抢票库存预热器。
     *
     * @param activityRepository 抢票活动仓储
     * @param stringRedisTemplate Redis 字符串模板
     */
    public SeckillStockWarmUpRunner(
            SeckillActivityRepository activityRepository,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.activityRepository = activityRepository;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 在应用启动后预热演示库存。
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        int warmUpCount = 0;
        for (SeckillActivityDTO activity : activityRepository.listActivities()) {
            // 只在 Redis 中不存在库存 Key 时写入演示库存，避免覆盖后续压测或人工准备的数据。
            stringRedisTemplate.opsForValue().setIfAbsent(
                    SeckillRedisKeySupport.buildStockKey(activity.getActivityId(), activity.getTicketId()),
                    String.valueOf(activity.getAvailableStock())
            );
            warmUpCount++;
        }
        log.info("抢票演示库存预热完成，activityCount={}", warmUpCount);
    }
}
