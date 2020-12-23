package com.yzh.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderSubmitVo {
    private Long addrId;//收货地址Id
    private Integer payType;//支付方式
    //去购物车再选一遍
    private String orderToken;//下次提交还要带上token
    private BigDecimal payprice;//验证价格
    //备注
    private String note;
}
