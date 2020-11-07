package com.yzh.mall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.yzh.mall.product.entity.AttrEntity;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class AtteGroupWithAttrsVo {

    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrEntities;
}
