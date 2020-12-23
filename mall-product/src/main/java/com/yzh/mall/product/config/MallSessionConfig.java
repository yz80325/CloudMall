package com.yzh.mall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class MallSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setDomainName("yzhmall.com");
        defaultCookieSerializer.setCookieName("YZHSESSION");
        return defaultCookieSerializer;
    }
    /**
     * 转Json
     *
     * */
    @Bean
    public RedisSerializer<Object> springSessionDefault(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
