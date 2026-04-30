package com.zhuanghd.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author: zhuanghongdong
 * Create_time: 2024/10/20
 * Description: redisson配置类
 */

@ConfigurationProperties(prefix = "vd.redis")
@Configuration
@Data
public class RedissonConfig {
    private String address;
    private String password;
    @Bean
    public RedissonClient redisClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(address).setPassword(password);
        return Redisson.create(config);
    }
}
