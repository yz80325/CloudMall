package com.yzh.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yzh.common.to.mq.SeckillOrderTo;
import com.yzh.common.utils.R;
import com.yzh.common.vo.MemberVo;
import com.yzh.mall.seckill.feign.CouponFeignService;
import com.yzh.mall.seckill.feign.ProductFeignService;
import com.yzh.mall.seckill.interceptor.LoginUserInterceptor;
import com.yzh.mall.seckill.service.SeckillService;
import com.yzh.mall.seckill.to.SecKillSkuRedisTo;
import com.yzh.mall.seckill.vo.SeckillSessionWithSkus;
import com.yzh.mall.seckill.vo.SeckillSkuVo;
import com.yzh.mall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImp implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService feignService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //活动信息头
    private final String SESSION_PREFIX="seckill:sessions:";

    //活动sku消息头
    private final String SKUSKILL_PREFIX="seckill:skus";

    //库存信号量
    private final String SKU_STOCK_SEMAPHORE="seckill:stock:";//+商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1.去扫描需要参与秒杀的活动
        R lates3DaysSession = couponFeignService.getLates3DaysSession();
        if (lates3DaysSession.getCode()==0){
            String s = JSON.toJSONString(lates3DaysSession.get("data"));
            List<SeckillSessionWithSkus> sessionWithSkusList = JSON.parseObject(s, new TypeReference<List<SeckillSessionWithSkus>>() {
            });
            //缓存到redis
            saveSessionInfos(sessionWithSkusList);
            saveSessionSkuInfo(sessionWithSkusList);
        }
    }

    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        long time = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSION_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSION_PREFIX, "");
            String[] s = replace.split("_");
            Long start=Long.parseLong(s[0]);
            Long end=Long.parseLong(s[1]);
            if (time>=start&&time<=end){
                //获取当前场的消息
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUSKILL_PREFIX);
                List<String> lists = hashOps.multiGet(range);
                if (lists!=null){
                    List<SecKillSkuRedisTo> collect = lists.stream().map(item -> {
                        SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(item.toString(), SecKillSkuRedisTo.class);
                        return secKillSkuRedisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }

        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> operations = stringRedisTemplate.boundHashOps(SKUSKILL_PREFIX);
        Set<String> keys = operations.keys();
        if (keys!=null){
            String regx="\\d_"+skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)){
                    String s = operations.get(key);
                    SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(s, SecKillSkuRedisTo.class);
                    long time = new Date().getTime();
                    Long startTime = secKillSkuRedisTo.getStartTime();
                    Long endTime = secKillSkuRedisTo.getEndTime();
                    if (time>=startTime&&time<=endTime){

                    }else {
                        secKillSkuRedisTo.setRandomCode(null);
                    }
                    return secKillSkuRedisTo;
                }
            }
        }
        return null;
    }

    /**
     * 秒杀
     * @param killId 商品信息 1_1
     * @param key 随机码
     * @param num 数量
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {
        MemberVo memberVo = LoginUserInterceptor.login.get();
        BoundHashOperations<String, String, String> operations = stringRedisTemplate.boundHashOps(SKUSKILL_PREFIX);
        //1_1
        String s = operations.get(killId);
        if (!StringUtils.isEmpty(s)){
            SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(s, SecKillSkuRedisTo.class);
            //1.校验合法性
            long now = new Date().getTime();
            Long startTime = secKillSkuRedisTo.getStartTime();
            Long endTime = secKillSkuRedisTo.getEndTime();
            //过期时间
            long TTl = endTime - startTime;
            if (now>=startTime&&now<=endTime){
                //2.随机码和商品id是否正确
                String id=secKillSkuRedisTo.getPromotionSessionId()+"_"+secKillSkuRedisTo.getSkuId();
                if (secKillSkuRedisTo.getRandomCode().equals(key)&&killId.equals(id)){
                    //验证购物数量是否合理
                    if (num<=secKillSkuRedisTo.getSeckillLimit()){
                        //验证是否已经购买过 幂等
                        String redisKey=memberVo.getId().toString()+"_"+id;
                        //自动过期
                        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), TTl, TimeUnit.MILLISECONDS);
                        if (aBoolean){
                            //从来没买过
                            //1.获取信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + key);
                            try {
                                if (semaphore.tryAcquire(num,100,TimeUnit.MILLISECONDS)){
                                    //秒杀成功
                                    //快速下单
                                    String timedId= IdWorker.getTimeId();
                                    SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                    seckillOrderTo.setOrderSn(timedId);
                                    seckillOrderTo.setMemberId(memberVo.getId());
                                    seckillOrderTo.setNum(num);
                                    seckillOrderTo.setPromotionSessionId(secKillSkuRedisTo.getPromotionSessionId());
                                    seckillOrderTo.setSkuId(secKillSkuRedisTo.getSkuId());
                                    seckillOrderTo.setSeckillPrice(seckillOrderTo.getSeckillPrice());
                                    rabbitTemplate.convertAndSend("order-event-exchange","order.esckill.order",seckillOrderTo);

                                    return timedId;
                                }else {
                                    return null;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else {
                            //买过了
                        }

                    }
                }else {
                    return null;
                }
            }else {
                return null;
            }

        }else {
            return null;
        }
        return null;
    }

    /**
     * 保存活动信息
     */
    public void saveSessionInfos(List<SeckillSessionWithSkus> seckillSessionWithSkus){
        seckillSessionWithSkus.stream().forEach(session->{
            long startTime = session.getCreateTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key=SESSION_PREFIX+startTime+"_"+endTime;
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (!hasKey){
                //此时间段有活动的skuId
                List<String> collect = session.getRelationEntities().stream().map(item->{
                    String s = item.getPromotionSessionId().toString()+"_"+item.getSkuId().toString();
                    return s;
                }).collect(Collectors.toList());

                stringRedisTemplate.opsForList().leftPushAll(key,collect);
            }

        });
    }
    /**
     * 保存商品信息
     */
    public void saveSessionSkuInfo(List<SeckillSessionWithSkus> seckillSessionWithSkus){

        seckillSessionWithSkus.stream().forEach(session->{
            BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(SKUSKILL_PREFIX);
            session.getRelationEntities().stream().forEach(seckillSkuVo -> {
                String token=null;
                Boolean hasKey = operations.hasKey(seckillSkuVo.getPromotionSessionId().toString()+"_"+seckillSkuVo.getSkuId().toString());
                if (!hasKey){
                    //缓存商品
                    SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                    //1.sku基本信息
                    R info = feignService.info(seckillSkuVo.getSkuId());
                    if (info.getCode()==0){
                        String s = JSON.toJSONString(info.get("skuInfo"));
                        SkuInfoVo skuInfoVo = JSON.parseObject(s, new TypeReference<SkuInfoVo>() {
                        });
                        secKillSkuRedisTo.setSkuInfoVo(skuInfoVo);
                    }
                    //2.sku秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo,secKillSkuRedisTo);

                    //秒杀时间
                    secKillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    secKillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    //随机码 秒杀开始时才开放
                    token = UUID.randomUUID().toString().replace("-", "");
                    secKillSkuRedisTo.setRandomCode(token);
                    String s = JSON.toJSONString(secKillSkuRedisTo);
                    operations.put(seckillSkuVo.getPromotionSessionId().toString()+"_"+seckillSkuVo.getSkuId().toString(),s);
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //商品秒杀的件数作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }
            });
        });
    }
}
