package com.yzh.mall.order.feign;

import com.yzh.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-product")
public interface ProductFeign {
    @RequestMapping("/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
