package com.yzh.mall.search.controller;

import com.yzh.common.exception.BizCodeEnum;
import com.yzh.common.to.es.SkuEsModel;
import com.yzh.common.utils.R;
import com.yzh.mall.search.service.SaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Service;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class EsSaveController {

    @Autowired
    SaveService saveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel>skuEsModels){
        boolean b=false;
        try {
            b=saveService.SaveSkuModel(skuEsModels);
        }catch (Exception e){
            log.error("Es商品上架错误{}",e);
            R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (!b){
            return R.ok();
        }else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }

    }
}
