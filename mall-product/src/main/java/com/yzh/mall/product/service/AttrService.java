package com.yzh.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.product.entity.AttrEntity;
import com.yzh.mall.product.entity.ProductAttrValueEntity;
import com.yzh.mall.product.vo.AttrGroupRelationVo;
import com.yzh.mall.product.vo.AttrResponseVo;
import com.yzh.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:53:30
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrResponseVo getAttrResponseVo(Long attrId);

    void updateAttrVo(AttrVo attr);

    List<AttrEntity> getRelationShip(Long attrId);

    void deleteRelation(AttrGroupRelationVo[] attrGroupRelation);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

