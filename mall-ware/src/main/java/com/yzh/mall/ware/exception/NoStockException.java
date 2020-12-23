package com.yzh.mall.ware.exception;

public class NoStockException extends RuntimeException{
    private Long skuId;
    public NoStockException(Long skuId){
        super(skuId+"没有库存");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
