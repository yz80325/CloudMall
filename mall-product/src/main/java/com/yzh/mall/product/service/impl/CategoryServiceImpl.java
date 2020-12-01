package com.yzh.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yzh.mall.product.entity.CategoryBrandRelationEntity;
import com.yzh.mall.product.service.CategoryBrandRelationService;
import com.yzh.mall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

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
                .map((menu) -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                }).sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
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
     */
    @Override
    public Long[] getCategoryId(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parenCatetId = getParenCatetId(catelogId, paths);
        Collections.reverse(parenCatetId);
        return paths.toArray(new Long[parenCatetId.size()]);
    }

    @CacheEvict(value = "category",allEntries = true)//失效模式
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }


    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public List<CategoryEntity> getCategory1() {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    //使用lettuce有堆外溢出异常，暂时转为jedis
    //@Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /**
         * 1,空结果缓存，解决缓存穿透
         * 2，设置过期时间（随机值），雪崩
         * 3，加锁，击穿
         */

        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            //缓存中没有
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();

            return catalogJsonFromDb;
        }
        //Json->Map<String, List<Catelog2Vo>>
        Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });

        return stringListMap;
    }

    //查询并封装整个数据
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {


        RLock lock = redissonClient.getLock("CatalogJson-Lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> res=null;
        try {
            res=getStringListMap();
        }finally {
            lock.unlock();
        }

        return res;

    }

    private Map<String, List<Catelog2Vo>> getStringListMap() {
        //加锁成功
        List<CategoryEntity> selectLists = baseMapper.selectList(null);
        //获取1级分类
        List<CategoryEntity> category1 = getParent_cid(selectLists, 0L);
        //获取Map<String,List<Catalog2Vo>>
        Map<String, List<Catelog2Vo>> collect = category1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //查询出对应的2及分类
            List<CategoryEntity> entities = getParent_cid(selectLists, v.getCatId());
            //封装
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(item -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                    //找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getParent_cid(selectLists, item.getCatId());
                    if (level3 != null) {
                        List<Catelog2Vo.Catalog3Vo> collect3 = level3.stream().map(l3 -> {
                            Catelog2Vo.Catalog3Vo catalog3Vo = new Catelog2Vo.Catalog3Vo(item.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect3);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        String s = JSON.toJSONString(collect);
        stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return collect;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryEntities, Long parent_cid) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> {
            return item.getParentCid() == parent_cid;
        }).collect(Collectors.toList());
        return collect;
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", parent_cid));
    }

    /**
     * 寻找父类分类Id
     */
    private List<Long> getParenCatetId(Long cateId, List<Long> paths) {
        paths.add(cateId);
        CategoryEntity byId = this.getById(cateId);
        if (byId.getParentCid() != 0) {
            getParenCatetId(byId.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((child) -> {
            child.setChildren(getChildrens(child, all));
            return child;
        }).sorted((child1, child2) -> {
            return (child1.getSort() == null ? 0 : child1.getSort()) - (child2.getSort() == null ? 0 : child2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}