package com.yzh.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yzh.common.utils.R;
import com.yzh.mall.product.entity.SkuImagesEntity;
import com.yzh.mall.product.entity.SpuImagesEntity;
import com.yzh.mall.product.entity.SpuInfoDescEntity;
import com.yzh.mall.product.feign.SecKillFeign;
import com.yzh.mall.product.service.*;
import com.yzh.mall.product.vo.SKuRedisVO;
import com.yzh.mall.product.vo.SkuItemSaleAttrVo;
import com.yzh.mall.product.vo.SkuItemVo;
import com.yzh.mall.product.vo.SpuItemBaseAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.product.dao.SkuInfoDao;
import com.yzh.mall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    SecKillFeign secKillFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    wrapper.le("price", max);
                }
            } catch (Exception e) {

            }


        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo getItem(Long skuId) throws ExecutionException, InterruptedException {
        //初始化
        SkuItemVo skuItemVo = new SkuItemVo();

        //异步
        CompletableFuture<SkuInfoEntity> myinfo = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, threadPoolExecutor);
        CompletableFuture<Void> SaleFuture = myinfo.thenAcceptAsync((res) -> {
            //获取spu的销售组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttrVos(saleAttrVos);
        }, threadPoolExecutor);
        CompletableFuture<Void> DescFuture = myinfo.thenAcceptAsync(res -> {
            //spu介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> BaseFuture = myinfo.thenAcceptAsync(res -> {
            //获取spu参数信息
            List<SpuItemBaseAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> ImageFuture = CompletableFuture.runAsync(() -> {
            //获取sku图片
            List<SkuImagesEntity> images = skuImagesService.getSkuImageById(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        CompletableFuture<Void> seckill = CompletableFuture.runAsync(() -> {
            //获取sku是否参加秒杀信息
            R r = secKillFeign.getskuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                String s = JSON.toJSONString(r.get("data"));
                SKuRedisVO sKuRedisVO = JSON.parseObject(s, new TypeReference<SKuRedisVO>() {
                });
                skuItemVo.setSKuRedisVOS(sKuRedisVO);
            }
        }, threadPoolExecutor);


        //等待所有都完成
        CompletableFuture.allOf(SaleFuture, DescFuture, BaseFuture, ImageFuture,seckill).get();

        return skuItemVo;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        BigDecimal price = baseMapper.getPrice(skuId);
        return price;
    }

}