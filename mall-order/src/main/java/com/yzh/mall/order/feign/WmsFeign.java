package com.yzh.mall.order.feign;

import com.yzh.common.to.SkuHasStockVo;
import com.yzh.common.utils.R;
import com.yzh.mall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("mall-ware")
public interface WmsFeign {
    @PostMapping("/ware/waresku/hassstock")
    R<List<SkuHasStockVo>> getSkuStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("attrId") Long id);

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo skuLockVo);

    @GetMapping("/ware/wareinfo/test")
    //@RequiresPermissions("ware:wareinfo:list")
    void test();
}
