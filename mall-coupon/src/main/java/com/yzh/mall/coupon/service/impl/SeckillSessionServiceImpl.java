package com.yzh.mall.coupon.service.impl;

import com.yzh.mall.coupon.entity.SeckillSkuRelationEntity;
import com.yzh.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.coupon.dao.SeckillSessionDao;
import com.yzh.mall.coupon.entity.SeckillSessionEntity;
import com.yzh.mall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaysSession() {
        //计算之后三天

        List<SeckillSessionEntity> start_time = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        if (start_time!=null&&start_time.size()>0){
            List<SeckillSessionEntity> collect = start_time.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> promotion_id = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_id", id));
                session.setRelationEntities(promotion_id);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    //得到起始时间
    private String startTime(){
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now,min);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }
    //得到起始时间
    private String endTime(){
        LocalDate now = LocalDate.now();
        LocalDate after = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(after,max);
        String format = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}