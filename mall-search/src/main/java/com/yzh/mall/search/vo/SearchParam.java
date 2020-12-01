package com.yzh.mall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;
    //排序
    private String sort;
    //查询是否有货
    private Integer hasStock;
    //价格区间
    private String skuPrice;
    //品牌Id
    private List<Long> brandId;
    //属性
    private List<String> attrs;
    private Integer pageNum;
}
