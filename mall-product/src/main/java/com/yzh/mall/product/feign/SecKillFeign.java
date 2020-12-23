package com.yzh.mall.product.feign;

import com.yzh.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-seckill")
public interface SecKillFeign {
    @GetMapping("/sku/seckill/{skuId}")
    R getskuSeckillInfo(@PathVariable("skuId") Long skuId);
}
