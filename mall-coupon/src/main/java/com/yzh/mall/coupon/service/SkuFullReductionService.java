package com.yzh.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.to.SkuReductionTo;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:51:14
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuRelation(SkuReductionTo skuReductionTo);
}

