package com.yzh.mall.ware.vo;

import lombok.Data;

/**
 * 库存锁定结果
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean isLock;
}
