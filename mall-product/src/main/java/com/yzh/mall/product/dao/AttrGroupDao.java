package com.yzh.mall.product.dao;

import com.yzh.mall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzh.mall.product.vo.SkuItemVo;
import com.yzh.mall.product.vo.SpuItemBaseAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 属性分组
 * 
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:53:30
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemBaseAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("catalogId") Long catalogId,@Param("spuId") Long spuId);
}
