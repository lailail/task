package com.example.ticket.user.repository;

import com.example.ticket.user.support.UserSecurityConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;

/**
 * Redis 刷新令牌存储实现。
 * 用于在用户服务中保存 refresh token 的有效状态，支撑换新和吊销能力。
 */
@Repository
public class RedisRefreshTokenStore implements RefreshTokenStore {
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造 Redis 刷新令牌存储。
     *
     * @param stringRedisTemplate Redis 字符串模板
     */
    public RedisRefreshTokenStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 保存刷新令牌状态。
     *
     * @param userId 用户主键
     * @param tokenId 令牌唯一标识
     * @param expireAt 令牌过期时间
     */
    @Override
    public void save(Long userId, String tokenId, Instant expireAt) {
        Duration ttl = Duration.between(Instant.now(), expireAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        stringRedisTemplate.opsForValue().set(buildKey(userId, tokenId), "1", ttl);
    }

    /**
     * 校验刷新令牌状态是否存在。
     *
     * @param userId 用户主键
     * @param tokenId 令牌唯一标识
     * @return 是否存在
     */
    @Override
    public boolean exists(Long userId, String tokenId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(userId, tokenId)));
    }

    /**
     * 删除刷新令牌状态。
     *
     * @param userId 用户主键
     * @param tokenId 令牌唯一标识
     */
    @Override
    public void delete(Long userId, String tokenId) {
        stringRedisTemplate.delete(buildKey(userId, tokenId));
    }

    /**
     * 构造刷新令牌 Redis key。
     *
     * @param userId 用户主键
     * @param tokenId 令牌唯一标识
     * @return Redis key
     */
    private String buildKey(Long userId, String tokenId) {
        return UserSecurityConstants.REFRESH_TOKEN_KEY_PREFIX + userId + ":" + tokenId;
    }
}
