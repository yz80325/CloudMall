package com.yzh.mall.order.to;

import com.yzh.mall.order.entity.OrderEntity;
import com.yzh.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;//订单计算
    private BigDecimal fare;//运费
}
