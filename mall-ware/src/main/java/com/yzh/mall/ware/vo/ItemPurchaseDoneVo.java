package com.yzh.mall.ware.vo;

import lombok.Data;

@Data
public class ItemPurchaseDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
