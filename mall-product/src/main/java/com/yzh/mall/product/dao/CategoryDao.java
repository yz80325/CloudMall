package com.yzh.mall.product.dao;

import com.yzh.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:53:30
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

    String getCateName(Long catelogId);
}
