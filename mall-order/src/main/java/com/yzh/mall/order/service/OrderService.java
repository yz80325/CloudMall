package com.yzh.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.to.mq.SeckillOrderTo;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.order.entity.OrderEntity;
import com.yzh.mall.order.vo.OrderSubmitVo;
import com.yzh.mall.order.vo.OrederConfirmVo;
import com.yzh.mall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-10 21:36:59
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrederConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderBySn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    void createSeckillOrder(SeckillOrderTo seckillOrderTo);
}

