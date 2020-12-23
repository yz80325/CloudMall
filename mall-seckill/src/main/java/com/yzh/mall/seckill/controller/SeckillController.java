package com.yzh.mall.seckill.controller;

import com.yzh.common.utils.R;
import com.yzh.mall.seckill.service.SeckillService;
import com.yzh.mall.seckill.to.SecKillSkuRedisTo;
import com.yzh.mall.seckill.vo.SeckillSkuVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class SeckillController {
    @Autowired
    SeckillService seckillService;
    /**
     * 返回当前时间可以参与秒杀的商品信息
     */
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SecKillSkuRedisTo> vos=seckillService.getCurrentSeckillSkus();
        return R.ok().put("data",vos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    public R getskuSeckillInfo(@PathVariable("skuId") Long skuId){
        SecKillSkuRedisTo to=seckillService.getSkuSeckillInfo(skuId);
        return R.ok().put("data",to);
    }
    //前端发来请求
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId, @RequestParam("key") String key, @RequestParam("num") Integer num,
                          Model model){
        String orderSn=seckillService.kill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        //1.是否登录
        return "success";
    }


    @GetMapping("/hello")
    public String sential(){
        log.info("方法被调用");
        return "hello";
    }
}
