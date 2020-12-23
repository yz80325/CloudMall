package com.yzh.mall.product.service.impl;

import com.yzh.mall.product.vo.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.product.dao.SkuSaleAttrValueDao;
import com.yzh.mall.product.entity.SkuSaleAttrValueEntity;
import com.yzh.mall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        return baseMapper.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuAttrValuesAsString(Long skuId) {
        return baseMapper.selectAttrAsString(skuId);
    }

}