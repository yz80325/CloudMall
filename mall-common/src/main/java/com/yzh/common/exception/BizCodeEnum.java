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
    SMS_CODE_EXCEPTION(10002,"验证码获取频率太高请稍后再试"),
    TO_MANY_REQUEST(10003,"请求流量过大"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架失败"),
    USER_EXIST_EXCEPTION(15001,"用户存在异常"),
    PHONE_EXIST_EXCEPTION(15002,"手机号存在异常"),
    USERNAME_PASSWORD_EXCEPTION(15003,"用户名或者密码错误");

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
