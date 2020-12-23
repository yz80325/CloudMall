package com.yzh.mall.order.service.impl;

import com.rabbitmq.client.Channel;
import com.yzh.mall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.order.dao.OrderItemDao;
import com.yzh.mall.order.entity.OrderItemEntity;
import com.yzh.mall.order.service.OrderItemService;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    @RabbitListener(queues = {"java.news"})
    public void recieve(Message message, OrderReturnReasonEntity orderReturnReasonEntity, Channel channel){
        System.out.println(message);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //签收消息
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {

        }
    }



}