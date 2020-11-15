package com.yzh.mall.product.feign;

import com.yzh.common.to.SkuReductionTo;
import com.yzh.common.to.SpuBoundTo;
import com.yzh.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuRelation(@RequestBody SkuReductionTo skuReductionTo);
}
