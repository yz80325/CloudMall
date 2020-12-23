package com.yzh.mall.seckill.scheduled;

import com.yzh.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品上架
 * 每天晚上3点，上架
 */
@Slf4j
@Service
public class SeckillSkuScheduled {
    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock="seckill:upload:lock";
    //TODO 幂等性处理
    //@Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days(){
        log.info("上架秒杀消息");

        RLock lock = redissonClient.getLock(upload_lock);

        lock.lock(10, TimeUnit.MINUTES);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }


    }
}
