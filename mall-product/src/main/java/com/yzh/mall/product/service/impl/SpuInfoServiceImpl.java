package com.yzh.mall.product.service.impl;

import com.yzh.common.constant.ProductConstant;
import com.yzh.common.to.SkuHasStockVo;
import com.yzh.common.to.SkuReductionTo;
import com.yzh.common.to.SpuBoundTo;
import com.yzh.common.to.es.SkuEsModel;
import com.yzh.common.utils.R;
import com.yzh.mall.product.dao.SpuInfoDescDao;
import com.yzh.mall.product.entity.*;
import com.yzh.mall.product.feign.CouponFeignService;
import com.yzh.mall.product.feign.SearchFeignService;
import com.yzh.mall.product.feign.WareFeignService;
import com.yzh.mall.product.service.*;
import com.yzh.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

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
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSupIno(spuInfoEntity);
        //2.保存Spu的描述图片
        List<String> desacipt = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", desacipt));
        saveDescInfo.saveDescInfo(spuInfoDescEntity);
        //3.保存spu的图片集
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);
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
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu优惠信息失败");
        }

        //5.保存Sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && skus.size() != 0) {
            skus.forEach(item -> {
                String defaultImage = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
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
                }).filter(entity -> {
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
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuRelation(skuReductionTo);
                    if (r1.getCode() != 0) {
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
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", w).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void up(Long spuId) {

        //查出spu对应的sku信息
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkuBySpuId(spuId);

        List<Long> skuIdList = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //查询当前所有sku可以被检索的规格属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrIds=attrService.selectSearchAttrIds(attrIds);

        Set<Long> idSets=new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = productAttrValueEntities.stream().filter(item -> {
            return idSets.contains(item.getAttrId());
        }).map(attr -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(attr, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        //远程查询是否有库存
        Map<Long, Boolean> stockMap=null;
        try {
            R<List<SkuHasStockVo>> skuStock = wareFeignService.getSkuStock(skuIdList);
            stockMap = skuStock.getData().stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.isHasStock()));
        }catch (Exception e){
            log.error("有异常{}",e);
        }


        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(sku -> {
            //组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //查询品牌名字和分类的名字
            BrandEntity BrandInfo = brandService.getById(sku.getBrandId());
            //写入库存
            if (finalStockMap ==null){
                skuEsModel.setHasStock(true);
            }else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            skuEsModel.setBrandName(BrandInfo.getName());
            skuEsModel.setBrandImg(BrandInfo.getLogo());
            skuEsModel.setHotScore(0L);
            CategoryEntity CateEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(CateEntity.getName());
            //添加描述
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        //发送给ES
        R r=searchFeignService.productStatusUp(upProducts);
        if(r.getCode()==0){
            //成功 修改spu状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //失败
            //TODO 重复调用
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spuInfoEntity = baseMapper.selectById(spuId);
        return spuInfoEntity;
    }


}