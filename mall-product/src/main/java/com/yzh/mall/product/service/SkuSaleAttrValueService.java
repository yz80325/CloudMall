package com.yzh.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.product.entity.SkuSaleAttrValueEntity;
import com.yzh.mall.product.vo.SkuItemSaleAttrVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:53:30
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);

    List<String> getSkuAttrValuesAsString(Long skuId);
}

