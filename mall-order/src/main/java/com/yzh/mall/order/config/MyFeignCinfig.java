package com.yzh.mall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 拦截器
 * 解决头部丢失问题
 */
@Configuration
public class MyFeignCinfig {
    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //当前请求头
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes!=null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    String cookie = request.getHeader("Cookie");
                    //发送请求头
                    requestTemplate.header("Cookie",cookie);
                }

            }
        };
    }

}
