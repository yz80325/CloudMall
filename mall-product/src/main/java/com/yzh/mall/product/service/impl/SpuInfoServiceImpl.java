package com.yzh.mall.product.service.impl;

import com.yzh.common.to.SkuReductionTo;
import com.yzh.common.to.SpuBoundTo;
import com.yzh.common.utils.R;
import com.yzh.mall.product.dao.SpuInfoDescDao;
import com.yzh.mall.product.entity.*;
import com.yzh.mall.product.feign.CouponFeignService;
import com.yzh.mall.product.service.*;
import com.yzh.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService saveDescInfo;

    //图片信息
    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {

        //1.保存spu基本数据
        SpuInfoEntity spuInfoEntity=new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSupIno(spuInfoEntity);
        //2.保存Spu的描述图片
        List<String>desacipt=spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",desacipt));
        saveDescInfo.saveDescInfo(spuInfoDescEntity);
        //3.保存spu的图片集
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);
        //4.保存Spu的规格参数
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(item.getAttrId());
            productAttrValueEntity.setAttrName(attrService.getById(item.getAttrId()).getAttrName());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);

        //保存spu的积分信息
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode()!=0){
            log.error("远程保存spu优惠信息失败");
        }

        //5.保存Sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if(skus!=null&&skus.size()!=0){
            skus.forEach(item->{
                String defaultImage="";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg()==1){
                        defaultImage=image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(skuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.save(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                //图片属性
                List<SkuImagesEntity> Image_collect = item.getImages().stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();

                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(Image_collect);
                //TODO 没有图片路径的不保存

                //销售属性
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> Sku_Collection = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(Sku_Collection);

                //sku的优惠信息
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount()>0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){
                    R r1 = couponFeignService.saveSkuRelation(skuReductionTo);
                    if (r1.getCode()!=0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });

        }
    }

    @Override
    public void saveBaseSupIno(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }


    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String)params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",w).or().like("spu_name",key);
            });
        }
        String status = (String)params.get("status");
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }
        String brandId = (String)params.get("brandId");
        if (!StringUtils.isEmpty(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String catelogId = (String)params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }


}