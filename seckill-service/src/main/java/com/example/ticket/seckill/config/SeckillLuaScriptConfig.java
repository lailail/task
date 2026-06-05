package com.example.ticket.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

/**
 * 抢票 Lua 脚本配置类。
 * 用于把 Redis Lua 脚本从业务代码中剥离出来，统一通过资源文件方式装配。
 */
@Configuration
@EnableConfigurationProperties(SeckillLuaScriptProperties.class)
public class SeckillLuaScriptConfig {

    /**
     * 装配库存预扣脚本 Bean。
     *
     * @param properties Lua 脚本配置属性
     * @param resourceLoader Spring 资源加载器
     * @return 可执行的 Redis Lua 脚本对象
     */
    @Bean("reserveStockRedisScript")
    public DefaultRedisScript<List> reserveStockRedisScript(
            SeckillLuaScriptProperties properties,
            ResourceLoader resourceLoader
    ) {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setResultType(List.class);
        // 通过资源文件加载脚本，避免把长脚本常量直接写进 Java 业务类中。
        script.setScriptSource(new ResourceScriptSource(
                resourceLoader.getResource(properties.getReserveScriptLocation())
        ));
        return script;
    }
}
