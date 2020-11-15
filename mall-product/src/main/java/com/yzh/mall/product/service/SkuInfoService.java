package com.yzh.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:53:30
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);
}

