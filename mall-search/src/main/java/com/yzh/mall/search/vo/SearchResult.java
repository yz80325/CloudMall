package com.yzh.mall.search.vo;


import com.yzh.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {
    private List<SkuEsModel>products;
    //当前页
    private Integer pageNum;
    //总记录数
    private Long total;
    //总页码
    private Integer totalPage;

    //查询所设计到的所有品牌
    private List<BrandVo>brands;
    //所有分类
    private List<CatalogVo>catalogs;
    //属性
    private List<AttrVo>attrs;

    //面包屑
    private List<NavVo> NavVo=new ArrayList<>();
    @Data
    public static class NavVo{
        private String NavName;
        private String NavValue;
        private String link;
    }

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;

        private List<String>attrValue;
    }
}
