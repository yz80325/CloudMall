package com.yzh.mall.product.service.impl;

import com.yzh.mall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.product.dao.BrandDao;
import com.yzh.mall.product.entity.BrandEntity;
import com.yzh.mall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //添加条件检索
        String key= (String) params.get("key");
        QueryWrapper<BrandEntity> brandEntityQueryWrapper=new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)){
            brandEntityQueryWrapper.eq("brand_id",key).or().like("name",key);
        }

        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                brandEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        //保证关联字段一致
        this.updateById(brand);
        if (!StringUtils.isEmpty(brand.getName())){
            //同步更新其他表
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());

            //TODO 更新其他表
        }

    }

    @Override
    public List<BrandEntity> selectInfoByIds(List<Long> brandIds) {
        List<BrandEntity> brandEntityList = baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brandId", brandIds));

        return brandEntityList;
    }

}