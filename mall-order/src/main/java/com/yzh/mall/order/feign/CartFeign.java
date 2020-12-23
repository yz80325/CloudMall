package com.yzh.mall.order.feign;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.yzh.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("mall-cart")
public interface CartFeign {
    @GetMapping("/currentUserCartItem")
    List<OrderItemVo> getCartItem();
}
