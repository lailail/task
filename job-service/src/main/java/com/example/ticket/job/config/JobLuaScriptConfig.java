package com.example.ticket.job.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

/**
 * 任务服务 Lua 脚本配置类。
 * 用于把库存释放脚本从业务代码中剥离出来，统一通过资源文件装配。
 */
@Configuration
@EnableConfigurationProperties(JobLuaScriptProperties.class)
public class JobLuaScriptConfig {

    /**
     * 装配库存释放脚本 Bean。
     *
     * @param properties Lua 脚本配置属性
     * @param resourceLoader Spring 资源加载器
     * @return 可执行的 Redis Lua 脚本对象
     */
    @Bean("releaseStockRedisScript")
    public DefaultRedisScript<List> releaseStockRedisScript(
            JobLuaScriptProperties properties,
            ResourceLoader resourceLoader
    ) {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setResultType(List.class);
        script.setScriptSource(new ResourceScriptSource(
                resourceLoader.getResource(properties.getReleaseScriptLocation())
        ));
        return script;
    }
}
