package com.yzh.mall.coupon.dao;

import com.yzh.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:51:14
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
