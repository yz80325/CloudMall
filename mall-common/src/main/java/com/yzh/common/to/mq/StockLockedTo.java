package com.yzh.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    private Long id;//库存工作单
    private StockDetail detail;//详情Id
}
