package com.example.ticket.seckill.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 抢票 Lua 脚本配置属性。
 * 用于集中管理 Redis Lua 脚本资源路径，避免把脚本位置硬编码在业务网关实现中。
 */
@ConfigurationProperties(prefix = "ticket.seckill.lua")
public class SeckillLuaScriptProperties {
    private String reserveScriptLocation = "classpath:lua/seckill/reserve-stock.lua";

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
}
