package com.yzh.mall.product.feign;

import com.yzh.common.to.SkuHasStockVo;
import com.yzh.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hassstock")
    R<List<SkuHasStockVo>> getSkuStock(@RequestBody List<Long> skuIds);
}
