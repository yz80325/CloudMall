package com.yzh.mall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelloSchedule {

    @Async
    @Scheduled(cron = "* * * * * ?")
    public void Hello(){
        log.info("hello");
    }


}
