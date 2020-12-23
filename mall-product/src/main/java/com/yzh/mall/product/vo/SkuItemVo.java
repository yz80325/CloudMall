package com.yzh.mall.product.vo;

import com.yzh.mall.product.entity.SkuImagesEntity;
import com.yzh.mall.product.entity.SkuInfoEntity;
import com.yzh.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;
@Data
public class SkuItemVo {
    //sku基本信息获取 pms_sku_info
    SkuInfoEntity info;
    //sku图片
    List<SkuImagesEntity> images;
    //获取销售属性组合
    List<SkuItemSaleAttrVo>saleAttrVos;
    //获取spu介绍
    SpuInfoDescEntity desp;
    //获取spu的规格参数
    List<SpuItemBaseAttrGroupVo> groupAttrs;

    //秒杀消息
    SKuRedisVO sKuRedisVOS;


}
