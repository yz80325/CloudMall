package com.yzh.mall.ware.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.yzh.common.to.OrderTo;
import com.yzh.common.to.mq.StockDetail;
import com.yzh.common.to.mq.StockLockedTo;
import com.yzh.common.utils.R;
import com.yzh.mall.ware.constant.MqConstant;
import com.yzh.mall.ware.entity.WareOrderTaskDetailEntity;
import com.yzh.mall.ware.entity.WareOrderTaskEntity;
import com.yzh.mall.ware.service.WareSkuService;
import com.yzh.mall.ware.vo.OrderEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = MqConstant.STOCK_RELEASE_QUEUE)
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;


    /**
     * 库存自动解锁
     * @param stockLockedTo
     * @param message
     */
    @RabbitHandler
    public void handleStockLockerRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁消息");
        try {
            wareSkuService.unLockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     * 订单已经被关掉
     * @param orderTo
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo,Message message,Channel channel) throws IOException {
        try {
            wareSkuService.unLockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
