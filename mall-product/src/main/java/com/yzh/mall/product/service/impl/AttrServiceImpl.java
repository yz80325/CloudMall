package com.yzh.mall.product.service.impl;

import com.yzh.common.constant.ProductConstant;
import com.yzh.mall.product.dao.AttrAttrgroupRelationDao;
import com.yzh.mall.product.dao.AttrGroupDao;
import com.yzh.mall.product.dao.CategoryDao;
import com.yzh.mall.product.entity.*;
import com.yzh.mall.product.service.CategoryService;
import com.yzh.mall.product.vo.AttrGroupRelationVo;
import com.yzh.mall.product.vo.AttrResponseVo;
import com.yzh.mall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.product.dao.AttrDao;
import com.yzh.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity=new AttrEntity();
        //封装为Attr
        BeanUtils.copyProperties(attr,attrEntity);

        //1,保存基本数据
        this.save(attrEntity);
        if (attr.getAttrType()== ProductConstant.AttrType.ATTR_TYPE_BASE.getCode()&&attr.getAttrGroupId()!=null){
            //保存关联关系
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper=new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(type)?1:0);
        if (catelogId!=0){
            queryWrapper.eq("catelog_id",catelogId);
        }
        String key= (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            //attr_id  attr_name
            queryWrapper.and((wapper)->
                    wapper.eq("attr_id",key).or().like("attr_name",key));
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils=new PageUtils(page);
        //把封装好的page中的数值取出来
        List<AttrEntity> records=page.getRecords();
        List<AttrResponseVo> attr_group_id = records.stream().map((attrEntity) -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(records, attrResponseVo);
            //销售属性不存在分组
            if("base".equalsIgnoreCase(type)){
                AttrAttrgroupRelationEntity attrId = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrEntity.getAttrId()));
                if (attrId != null) {
                    Long attrGroupId = attrId.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }
            return attrResponseVo;
        }).collect(Collectors.toList());

        pageUtils.setList(attr_group_id);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrResponseVo(Long attrId) {
        AttrResponseVo attrResponseVo=new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,attrResponseVo);
        //设置分组信息
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if(attrAttrgroupRelationEntity!=null){
            attrResponseVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
            if (attrGroupEntity!=null){
                attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        //设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] categoryId = categoryService.getCategoryId(catelogId);
        attrResponseVo.setCatelogPath(categoryId);
        String cateName = categoryDao.getCateName(catelogId);
        attrResponseVo.setCatelogName(cateName);
        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttrVo(AttrVo attr) {
        AttrEntity attrEntity=new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);

        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());

        Integer attr_id_count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
        if (attr_id_count>0){
            attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity,new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()));
        }else {
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    /**
     * 根据分组Id查找关联的属性
     * @param attrId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationShip(Long attrId) {
        List<AttrAttrgroupRelationEntity> attr_group = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrId));
        List<Long> collect = attr_group.stream().map((relationEntity) -> {
            return relationEntity.getAttrId();
        }).collect(Collectors.toList());

        if (collect==null||collect.size()==0){
            return null;
        }
        Collection<AttrEntity> attrEntities = this.listByIds(collect);

        return (List<AttrEntity>) attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] attrGroupRelation) {
        List<AttrAttrgroupRelationEntity> collect = new ArrayList<AttrGroupRelationVo>(Arrays.asList(attrGroupRelation)).stream().map((item) -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelation(collect);
    }

    /**
     * 获取当前分组没关联的分类
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //当前分组只能关联自己所属的分类里的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //当前分组只能关联别的所属的分组没有引用的属性
        //当前分类下的其他分组
        List<AttrGroupEntity> grouop = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = grouop.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //这些分组关联的属性
        List<AttrAttrgroupRelationEntity> attr_group_id = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> Attr_Ids = attr_group_id.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //移除这些属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrType.ATTR_TYPE_BASE.getCode());
        if (Attr_Ids!=null||Attr_Ids.size()>0){
            queryWrapper.notIn("attr_id", Attr_Ids);
        }
        String key= (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and((w)->
                    w.eq("attr_id",key).or().like("attr_name",key));
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }



}