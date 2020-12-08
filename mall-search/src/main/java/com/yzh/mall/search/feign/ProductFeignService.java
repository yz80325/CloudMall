package com.yzh.mall.search.feign;

import com.yzh.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("mall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    R info(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R getInfoByIds(@RequestParam("brandIds") List<Long> brandIds);
}
