package com.yzh.mall.product.service.impl;

import com.yzh.mall.product.entity.AttrEntity;
import com.yzh.mall.product.service.AttrService;
import com.yzh.mall.product.service.SkuImagesService;
import com.yzh.mall.product.vo.AtteGroupWithAttrsVo;
import com.yzh.mall.product.vo.SkuItemVo;
import com.yzh.mall.product.vo.SpuItemBaseAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.product.dao.AttrGroupDao;
import com.yzh.mall.product.entity.AttrGroupEntity;
import com.yzh.mall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catId) {
        //如果是0，就全查询
        if (catId==0){
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    new QueryWrapper<AttrGroupEntity>()
            );
            return new PageUtils(page);
        }else {
            //如果有查询条件,先获取查询key
            String key = (String) params.get("key");
            QueryWrapper<AttrGroupEntity> wapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId);
            if (!StringUtils.isEmpty(key)){
                wapper.and((obj)->
                {
                    obj.eq("attr_group_id",key).or().like("attr_group_name",key);
                });
            }
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AtteGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogid) {
        List<AttrGroupEntity> catelog_group = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogid));

        //查出所有属性
        List<AtteGroupWithAttrsVo> collect = catelog_group.stream().map(group -> {
            AtteGroupWithAttrsVo attrsVo = new AtteGroupWithAttrsVo();
            BeanUtils.copyProperties(group, attrsVo);
            List<AttrEntity> relationShip = attrService.getRelationShip(attrsVo.getAttrGroupId());
            attrsVo.setAttrEntities(relationShip);
            return attrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpuItemBaseAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        //查出当前spu对应得信息
        AttrGroupDao baseMapper = this.baseMapper;
        List<SpuItemBaseAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;
    }


}