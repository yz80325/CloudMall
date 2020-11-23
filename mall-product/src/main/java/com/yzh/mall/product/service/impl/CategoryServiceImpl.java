package com.yzh.mall.product.service.impl;

import com.yzh.mall.product.entity.CategoryBrandRelationEntity;
import com.yzh.mall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.product.dao.CategoryDao;
import com.yzh.mall.product.entity.CategoryEntity;
import com.yzh.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //查出一级分类，parentid=0
        List<CategoryEntity> Level1 = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0)
                .map((menu)->{
                    menu.setChildren(getChildrens(menu,entities));
                    return menu;
                }).sorted((menu1,menu2)->{
                    return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
                })
                .collect(Collectors.toList());

        //查出一级分类的子分类

        return Level1;
    }

    @Override
    public void removeMenuById(List<Long> ids) {
        //TODO 删除时检查别的地方是否调用
        baseMapper.deleteBatchIds(ids);
    }

    /**
     * 获取分类
     * [父分类/子/孙]
     *
     * */
    @Override
    public Long[] getCategoryId(Long catelogId) {
        List<Long> paths=new ArrayList<>();
        List<Long> parenCatetId = getParenCatetId(catelogId, paths);
        Collections.reverse(parenCatetId);
        return paths.toArray(new Long[parenCatetId.size()]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
        }
    }

    @Override
    public List<CategoryEntity> getCategory1() {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    /**
     * 寻找父类分类Id
     * */
    private List<Long> getParenCatetId(Long cateId,List<Long> paths){
        paths.add(cateId);
        CategoryEntity byId = this.getById(cateId);
        if (byId.getParentCid()!=0){
            getParenCatetId(byId.getParentCid(),paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((child) -> {
            child.setChildren(getChildrens(child, all));
            return child;
        }).sorted((child1, child2) -> {
            return (child1.getSort()==null?0:child1.getSort()) - (child2.getSort()==null?0:child2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}