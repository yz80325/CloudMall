package com.yzh.mall.seckill.feign;

import com.yzh.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("mall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/last3DaySession")
    R getLates3DaysSession();

}
