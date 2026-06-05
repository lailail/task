package com.example.ticket.common.auth;

/**
 * 已认证用户对象。
 * 用于承载网关验签后向下游透传的最小身份事实，避免业务服务继续信任前端直传身份。
 */
public class AuthenticatedUser {
    private Long userId;
    private String username;
    private String displayName;
    private String tokenId;

    /**
     * 获取用户主键。
     *
     * @return 用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户主键。
     *
     * @param userId 用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取展示名称。
     *
     * @return 展示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置展示名称。
     *
     * @param displayName 展示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取令牌唯一标识。
     *
     * @return 令牌唯一标识
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * 设置令牌唯一标识。
     *
     * @param tokenId 令牌唯一标识
     */
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
