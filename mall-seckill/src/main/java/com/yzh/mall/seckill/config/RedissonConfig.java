package com.yzh.mall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redssion(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.22.129:6379");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
