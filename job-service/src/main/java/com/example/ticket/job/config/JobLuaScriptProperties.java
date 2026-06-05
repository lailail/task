package com.example.ticket.job.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务服务 Lua 脚本配置属性。
 * 用于承载库存释放脚本的资源位置，避免脚本路径硬编码在配置类中。
 */
@ConfigurationProperties(prefix = "ticket.job.lua")
public class JobLuaScriptProperties {
    private String releaseScriptLocation;

    /**
     * 获取释放脚本资源位置。
     *
     * @return 释放脚本资源位置
     */
    public String getReleaseScriptLocation() {
        return releaseScriptLocation;
    }

    /**
     * 设置释放脚本资源位置。
     *
     * @param releaseScriptLocation 释放脚本资源位置
     */
    public void setReleaseScriptLocation(String releaseScriptLocation) {
        this.releaseScriptLocation = releaseScriptLocation;
    }
}
