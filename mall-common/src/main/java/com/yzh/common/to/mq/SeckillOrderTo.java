package com.yzh.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderTo {
    private String orderSn;//订单号
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer num;
    private Long memberId;//哪个人购买的
}
