package com.yzh.mall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class MySessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setDomainName("yzhmall.com");
        defaultCookieSerializer.setCookieName("YZHSESSION");
        return defaultCookieSerializer;
    }

    @Bean
    public RedisSerializer<Object>springSessionDefualt(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
