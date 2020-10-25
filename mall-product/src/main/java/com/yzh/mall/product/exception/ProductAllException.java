package com.yzh.mall.product.exception;

import com.yzh.common.exception.BizCodeEnum;
import com.yzh.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.yzh.mall.product.controller")
public class ProductAllException {

    //字符验证异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.error("数据校验有问题{},异常类型{}",e.getMessage(),e.getClass());
        Map<String,String> map=new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((item)->
        {
            String messqage=item.getDefaultMessage();
            String Filed=item.getField();
            map.put(Filed,messqage);
        });
        return R.error(BizCodeEnum.VAILD_EXAPTION.getCode(), BizCodeEnum.VAILD_EXAPTION.getMessage()).put("data",map);
    }

    //其他异常
    @ExceptionHandler(Throwable.class)
    public R handleException(Throwable throwable){
        log.error("error",throwable);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMessage());
    }
}
