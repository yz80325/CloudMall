package com.yzh.mall.order.controller;

import java.util.Arrays;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yzh.mall.order.entity.OrderEntity;
import com.yzh.mall.order.service.OrderService;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.R;



/**
 * 订单
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-10 21:36:59
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;


    @GetMapping("/status/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn){
        OrderEntity orderEntity=orderService.getOrderBySn(orderSn);
        return R.ok().put("order",orderEntity);
    }
    @RequestMapping("/test/order")
    public R memberOrder(){
        OrderEntity orderEntity=new OrderEntity();
        orderEntity.setBillContent("aaaa");
        return R.ok().put("order",Arrays.asList(orderEntity));
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
   // @RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
  //  @RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
  //  @RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
