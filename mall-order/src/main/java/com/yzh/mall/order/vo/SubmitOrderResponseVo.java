package com.yzh.mall.order.vo;

import com.yzh.mall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity orderEntity;
    private Integer code;//错误状态吗 0：成功

}
