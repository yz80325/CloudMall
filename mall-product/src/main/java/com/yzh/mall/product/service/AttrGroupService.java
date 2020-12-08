package com.yzh.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.product.entity.AttrGroupEntity;
import com.yzh.mall.product.vo.AtteGroupWithAttrsVo;
import com.yzh.mall.product.vo.SkuItemVo;
import com.yzh.mall.product.vo.SpuItemBaseAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:53:30
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catId);


    List<AtteGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogid);

    List<SpuItemBaseAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

