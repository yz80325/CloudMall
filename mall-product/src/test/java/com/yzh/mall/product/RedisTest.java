package com.yzh.mall.product;

import com.yzh.mall.product.config.RedissonConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void TestRedis(){
        System.out.println(redissonClient);
    }

    @Test
    public void Test(){
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        stringStringValueOperations.set("hello","world_"+ UUID.randomUUID().toString());
        //查询
        String hello = stringStringValueOperations.get("hello");
        System.out.println(hello);
    }
}
