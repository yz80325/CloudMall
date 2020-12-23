package com.yzh.mall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


    /**
     * 创建交换机
     */
    @Bean
    public Exchange stockExchange(){
        TopicExchange topicExchange = new TopicExchange("stock-event-exchange", true, false);
        return topicExchange;
    }

    /**
     * 创建队列
     */
    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue",true,false,false);
    }

    /**
     * 创建延迟队列
     */
    @Bean
    public Queue stockDelayQueue(){
        /**
         * 设置队列属性
         */
        Map<String,Object> arguments=new HashMap<>();
        //死信交换机
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        //死信路由件
        arguments.put("x-dead-letter-routing-key","stock.release");
        //时间
        arguments.put("x-message-ttl",120000);
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }

    /**
     * 与普通队列绑定
     * @return
     */
    @Bean
    public Binding stockReleaseBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }

    @Bean
    public Binding stockLockedBinding(){
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }


}
