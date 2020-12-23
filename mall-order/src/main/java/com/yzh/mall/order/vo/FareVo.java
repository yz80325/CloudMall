package com.yzh.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class FareVo {
    private MemeberAddressVo addressVo;
    private BigDecimal Fare;
}
