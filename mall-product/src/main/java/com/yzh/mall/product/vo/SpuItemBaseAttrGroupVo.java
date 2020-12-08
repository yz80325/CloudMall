package com.yzh.mall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SpuItemBaseAttrGroupVo {
    private String groupName;
    private List<SkuBaseAttrVo> attrs;
}
