package com.yzh.mall.order.vo;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class OrederConfirmVo {
    @Getter @Setter
    //用户地址值
    List<MemeberAddressVo> addressVos;
    @Getter @Setter
    //选中所有购物享
    List<OrderItemVo> orderItems;
    @Getter @Setter
    //优惠劵信息
    Integer integration;

    @Getter @Setter
    Map<Long,Boolean>stocks;

    @Getter @Setter
    //防重令牌
    String orderToken;

    public BigDecimal getTotal() {
        BigDecimal bigDecimal = new BigDecimal("0");
        if (orderItems!=null&&orderItems.size()>0){
            for (OrderItemVo orderItem : orderItems) {
                BigDecimal multiply = orderItem.getPrice().multiply(new BigDecimal(orderItem.getCount().toString()));
                BigDecimal add = bigDecimal.add(multiply);
            }
        }
        return bigDecimal;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }

}
