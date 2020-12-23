package com.yzh.mall.order.listener;

import com.rabbitmq.client.Channel;
import com.yzh.common.to.mq.SeckillOrderTo;
import com.yzh.mall.order.entity.OrderEntity;
import com.yzh.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {
        log.info("创建秒杀订单");
        orderService.createSeckillOrder(seckillOrderTo);
        //手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
