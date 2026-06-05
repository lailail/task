package com.example.ticket.user.repository;

import java.time.Instant;

/**
 * 刷新令牌存储接口。
 * 用于抽象 refresh token 的状态保存和吊销行为，避免服务层直接耦合具体缓存实现。
 */
public interface RefreshTokenStore {

    /**
     * 保存刷新令牌状态。
     *
     * @param userId 用户主键
     * @param tokenId 令牌唯一标识
     * @param expireAt 令牌过期时间
     */
    void save(Long userId, String tokenId, Instant expireAt);

    /**
     * 校验刷新令牌当前是否仍有效。
     *
     * @param userId 用户主键
     * @param tokenId 令牌唯一标识
     * @return 是否有效
     */
    boolean exists(Long userId, String tokenId);

    /**
     * 吊销刷新令牌。
     *
     * @param userId 用户主键
     * @param tokenId 令牌唯一标识
     */
    void delete(Long userId, String tokenId);
}
