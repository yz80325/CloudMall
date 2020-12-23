package com.yzh.mall.ware.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yzh.mall.ware.service.WareSkuService;
import com.yzh.mall.ware.vo.FareVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yzh.mall.ware.entity.WareInfoEntity;
import com.yzh.mall.ware.service.WareInfoService;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.R;



/**
 * 仓库信息
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:42:51
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    @Autowired
    WareSkuService wareSkuService;


    /**
     * 运费
     *
     * @param id
     * @return
     */
    @GetMapping("/fare")
    public R getFare(@RequestParam("attrId") Long id){
        FareVo fare =wareInfoService.getFare(id);
        return R.ok().put("data",fare);
    }

    @GetMapping("/test")
    //@RequiresPermissions("ware:wareinfo:list")
    public void test(){
        wareSkuService.test();

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:wareinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
   // @RequiresPermissions("ware:wareinfo:info")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
 //   @RequiresPermissions("ware:wareinfo:save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
 //   @RequiresPermissions("ware:wareinfo:update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
 //   @RequiresPermissions("ware:wareinfo:delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
