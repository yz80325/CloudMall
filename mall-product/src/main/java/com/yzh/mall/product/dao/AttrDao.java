package com.yzh.mall.product.dao;

import com.yzh.mall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:53:30
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearchablId(@Param("attrIds") List<Long> attrIds);
}
