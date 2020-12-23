package com.yzh.mall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yzh.mall.ware.vo.LockStockResult;
import com.yzh.mall.ware.vo.SkuHasStockVo;
import com.yzh.mall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yzh.mall.ware.entity.WareSkuEntity;
import com.yzh.mall.ware.service.WareSkuService;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.R;



/**
 * 商品库存
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:42:51
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo skuLockVo){
        boolean results=false;
        try {
            results=wareSkuService.lockStock(skuLockVo);
        }catch (Exception e){
        }
        if (results==true){
            return R.ok();
        }
        return R.error(21000,"库存不足");
    }
    /**
     * 查询Sku是否有库存
     */
    @PostMapping("/hassstock")
    public R<List<SkuHasStockVo>> getSkuStock(@RequestBody List<Long> skuIds){
        List<SkuHasStockVo> skuHasStockVo=wareSkuService.getSkusHasStack(skuIds);
        R<List<SkuHasStockVo>> ok = R.ok();
        ok.setData(skuHasStockVo);
        return ok;
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
   // @RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
 //   @RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
 //   @RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
 //   @RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
