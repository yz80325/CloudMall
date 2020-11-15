package com.yzh.mall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yzh.common.to.SkuReductionTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yzh.mall.coupon.entity.SkuFullReductionEntity;
import com.yzh.mall.coupon.service.SkuFullReductionService;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.R;



/**
 * 商品满减信息
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:51:14
 */
@RestController
@RequestMapping("coupon/skufullreduction")
public class SkuFullReductionController {
    @Autowired
    private SkuFullReductionService skuFullReductionService;

    /**
     * 列表
     */
    @PostMapping("/saveinfo")
    //@RequiresPermissions("coupon:skufullreduction:list")
    public R list(@RequestBody SkuReductionTo skuReductionTo){

        skuFullReductionService.saveSkuRelation(skuReductionTo);

        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
   // @RequiresPermissions("coupon:skufullreduction:info")
    public R info(@PathVariable("id") Long id){
		SkuFullReductionEntity skuFullReduction = skuFullReductionService.getById(id);

        return R.ok().put("skuFullReduction", skuFullReduction);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
 //   @RequiresPermissions("coupon:skufullreduction:save")
    public R save(@RequestBody SkuFullReductionEntity skuFullReduction){
		skuFullReductionService.save(skuFullReduction);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
 //   @RequiresPermissions("coupon:skufullreduction:update")
    public R update(@RequestBody SkuFullReductionEntity skuFullReduction){
		skuFullReductionService.updateById(skuFullReduction);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
 //   @RequiresPermissions("coupon:skufullreduction:delete")
    public R delete(@RequestBody Long[] ids){
		skuFullReductionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
