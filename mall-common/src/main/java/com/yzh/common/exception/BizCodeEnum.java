package com.yzh.common.exception;


/**
 * 10:通用
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 *
 * */
public enum  BizCodeEnum {

    UNKNOW_EXCEPTION(10000,"未知异常"),
    VAILD_EXAPTION(10001,"参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架失败");

    private int code;
    private String message;

    BizCodeEnum(int code,String message){
        this.code=code;
        this.message=message;
    }

    public int getCode(){
        return code;
    }
    public String getMessage(){
        return message;
    }



}
