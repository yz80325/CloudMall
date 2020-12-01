package com.yzh.mall.product.web;

import com.yzh.mall.product.entity.CategoryEntity;
import com.yzh.mall.product.service.CategoryService;
import com.yzh.mall.product.vo.Catelog2Vo;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {


    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        List<CategoryEntity> category1=categoryService.getCategory1();

        model.addAttribute("categorys",category1);
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String,List<Catelog2Vo>> getCatalogJson(){
        Map<String,List<Catelog2Vo>> map=categoryService.getCatalogJson();
        return map;
    }

    @GetMapping("/hello")
    public String Hello(Model model){

        return "hello";
    }

    @GetMapping("/write")
    public void Write(){
        RReadWriteLock rw = redissonClient.getReadWriteLock("rw");
        rw.writeLock().lock();
        String s = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set("write",s);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rw.writeLock().unlock();

    }

    @GetMapping("/read")
    @ResponseBody
    public String Read(){
        RReadWriteLock rw = redissonClient.getReadWriteLock("rw");
        rw.readLock().lock();
        String write = stringRedisTemplate.opsForValue().get("write");
        rw.readLock().unlock();
        return write;
    }

}
