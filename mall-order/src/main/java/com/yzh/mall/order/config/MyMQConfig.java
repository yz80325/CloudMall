package com.yzh.mall.order.config;

import com.rabbitmq.client.Channel;
import com.yzh.mall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {


    /**
     * 创建队列
     * 延迟队列
     * @Bean 自动创建
     */
    @Bean
    public Queue orderDelayQueue(){
        /**
         * 设置队列属性
         */
        Map<String,Object> arguments=new HashMap<>();
        //死信交换机
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        //死信路由件
        arguments.put("x-dead-letter-routing-key","order.release.order");
        //时间
        arguments.put("x-message-ttl",60000);
        Queue queue = new Queue("order.delay.queue", true, false, false,arguments);


        return queue;
    }

    /**
     * 释放队列
     * @return
     */
    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return queue;
    }
    @Bean
    public Exchange orderEventExchange(){
        TopicExchange topicExchange = new TopicExchange("order-event-exchange", true, false);
        return topicExchange;
    }
    @Bean
    public Binding orderCreateBinding(){
       return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null
                );
    }
    @Bean
    public Binding orderReleaseBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null
        );
    }

    /**
     * 将订单释放与库存释放进行绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order.#",
                null
        );
    }

    //秒杀队列
    @Bean
    public Queue orderSeckillOrderQueue(){
        return new Queue("order.seckill.order.queue",true,false,false);
    }

    //String destination, Binding.DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
    @Bean
    public Binding orderSeckillOrderQueueBinding(){
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }

}
