package com.example.ticket.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关认证配置属性。
 * 用于管理网关匿名放行路径，避免把路径白名单硬编码在过滤器中。
 */
@ConfigurationProperties(prefix = "ticket.security.gateway")
public class GatewayAuthProperties {
    private List<String> permitPaths = new ArrayList<>();

    /**
     * 获取匿名放行路径列表。
     *
     * @return 匿名放行路径列表
     */
    public List<String> getPermitPaths() {
        return permitPaths;
    }

    /**
     * 设置匿名放行路径列表。
     *
     * @param permitPaths 匿名放行路径列表
     */
    public void setPermitPaths(List<String> permitPaths) {
        this.permitPaths = permitPaths;
    }
}
