package com.example.ticket.seckill.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 抢票 Lua 脚本配置属性。
 * 用于集中管理 Redis Lua 脚本资源路径，避免把脚本位置硬编码在业务网关实现中。
 */
@ConfigurationProperties(prefix = "ticket.seckill.lua")
public class SeckillLuaScriptProperties {
    private String reserveScriptLocation = "classpath:lua/seckill/reserve-stock.lua";
    private String rollbackScriptLocation = "classpath:lua/seckill/rollback-reserve-stock.lua";

    /**
     * 获取库存预扣 Lua 脚本位置。
     *
     * @return 脚本资源位置
     */
    public String getReserveScriptLocation() {
        return reserveScriptLocation;
    }

    /**
     * 设置库存预扣 Lua 脚本位置。
     *
     * @param reserveScriptLocation 脚本资源位置
     */
    public void setReserveScriptLocation(String reserveScriptLocation) {
        this.reserveScriptLocation = reserveScriptLocation;
    }

    /**
     * 获取预扣失败回滚 Lua 脚本位置。
     *
     * @return 脚本资源位置
     */
    public String getRollbackScriptLocation() {
        return rollbackScriptLocation;
    }

    /**
     * 设置预扣失败回滚 Lua 脚本位置。
     *
     * @param rollbackScriptLocation 脚本资源位置
     */
    public void setRollbackScriptLocation(String rollbackScriptLocation) {
        this.rollbackScriptLocation = rollbackScriptLocation;
    }
}
