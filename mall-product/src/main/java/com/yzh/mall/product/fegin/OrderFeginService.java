package com.yzh.mall.product.fegin;

import com.yzh.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-order")
public interface OrderFeginService {

    @RequestMapping("/order/order/test/order")
    public R memberOrder();
}
