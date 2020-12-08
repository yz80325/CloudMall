package com.yzh.mall.search.service.Imp;

import com.alibaba.fastjson.JSON;
import com.yzh.common.to.AttrResponseVo;
import com.yzh.common.to.es.SkuEsModel;

import com.yzh.common.utils.R;
import com.yzh.mall.search.config.MallElasticSearchConfig;
import com.yzh.mall.search.constant.EsContant;
import com.yzh.mall.search.entity.BrandEntity;
import com.yzh.mall.search.feign.ProductFeignService;
import com.yzh.mall.search.service.MallSearchService;
import com.yzh.mall.search.vo.SearchParam;
import com.yzh.mall.search.vo.SearchResult;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImp implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        //准备检索请求
        SearchRequest searchRequest=buildSearchRequest(param);
        SearchResponse response;
        SearchResult result = null;
        try {
            response= client.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
            result=buildSearchResult(response,param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {
        SearchResult searchResult=new SearchResult();

        SearchHits hits = response.getHits();
        List<SkuEsModel>esModels=new ArrayList<>();
        if (hits.getHits()!=null&&hits.getHits().length>0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel= JSON.parseObject(sourceAsString,SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                esModels.add(skuEsModel);
            }
        }
        //商品信息
        searchResult.setProducts(esModels);
        //属性信息
        List<SearchResult.AttrVo> attrVos=new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg=attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //属性id
            long id = bucket.getKeyAsNumber().longValue();
            //属性名字
            String attr_name = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            //属性值
            List<String> attr_values = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(id);
            attrVo.setAttrName(attr_name);
            attrVo.setAttrValue(attr_values);
            attrVos.add(attrVo);

        }
        searchResult.setAttrs(attrVos);
        //所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo>catalogVos=new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            //分类ID
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogname = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogname);
            catalogVos.add(catalogVo);
        }
        //品牌信息
        List<SearchResult.BrandVo>brandVos=new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<? extends Terms.Bucket> brand_buckets = brand_agg.getBuckets();
        for (Terms.Bucket brand_bucket : brand_buckets) {
            SearchResult.BrandVo brandVo=new SearchResult.BrandVo();
            //得到id
            long id = brand_bucket.getKeyAsNumber().longValue();
            //得到名
            String brand_name = ((ParsedStringTerms) brand_bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            //得到图片
            String brand_img = ((ParsedStringTerms) brand_bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(id);
            brandVo.setBrandImg(brand_img);
            brandVo.setBrandName(brand_name);
            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);
        //设置分类
        searchResult.setCatalogs(catalogVos);
        //当前页码
        searchResult.setPageNum(param.getPageNum());
        //总记录数
        long totalHits = hits.getTotalHits().value;
        searchResult.setTotal(totalHits);
        //总页码
        int totalPage=(int)totalHits%EsContant.PRODUCT_PAGESIZE==0?(int)totalHits%EsContant.PRODUCT_PAGESIZE:((int)totalHits%EsContant.PRODUCT_PAGESIZE)+1;
        searchResult.setTotalPage(totalPage);

        if (param.getAttrs()!=null&&param.getAttrs().size()>0){
            //面包
            List<SearchResult.NavVo> naves = param.getAttrs().stream().map(attr -> {
                //分析每一个传来的参数值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R info = productFeignService.info(Long.parseLong(s[0]));
                if (info.getCode() == 0) {
                    AttrResponseVo attrResponseVo = (AttrResponseVo) info.get("attr");
                    if (attrResponseVo != null) {
                        navVo.setNavName(attrResponseVo.getAttrName());
                    }
                } else {
                    navVo.setNavName(s[0]);
                }
                String replace = replaceQueryString(param, attr,"attr");
                navVo.setLink("http://search.yzhmall.com/list.html?" + replace);

                return navVo;

            }).collect(Collectors.toList());
            searchResult.setNavVo(naves);
        }

        //品牌封装
        if(param.getBrandId()!=null&&param.getBrandId().size()>0){
            List<SearchResult.NavVo> navVos = searchResult.getNavVo();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R infoByIds = productFeignService.getInfoByIds(param.getBrandId());
            if(infoByIds.getCode()==0){
                List<BrandEntity> brandEntities= (List<BrandEntity>) infoByIds.get("brands");
                StringBuffer buffer=new StringBuffer();
                String replace="";
                for (BrandEntity brandEntity : brandEntities) {
                    buffer= buffer.append(brandEntity.getName() + ";");
                     replace= replaceQueryString(param,brandEntity.getBrandId()+"","brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.yzhmall.com/list.html?" + replace);
            }

            navVos.add(navVo);
        }

        return searchResult;
    }

    private String replaceQueryString(SearchParam param, String attr,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");
            encode=encode.replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //取消掉面包屑跳转路径
        String replace = param.getQueryString().replace("&"+key+"=" + encode, "");
        return replace;
    }

    /**
     * 检索请求
     * @return
     * @param param
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        //构建DSL语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构建bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1must
        if (!StringUtils.isEmpty(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        //1.2 filter 三级分类
        if (param.getCatalog3Id()!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //1.2 filter 按照品牌
        if (param.getBrandId()!=null&&param.getBrandId().size()>0){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId",param.getBrandId()));
        }
        //1.2 属性查询 聚合
        if (param.getAttrs()!=null&&param.getAttrs().size()>0){
            for (String attr:param.getAttrs()){
                //attrs=1_5寸：8寸&&attrs=2_16G:8G
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                //属性Id
                String[] attrs = attr.split("_");
                String attrId=attrs[0];
                //检索值
                String[] attrValues = attrs[1].split(":");
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                //每一个必须生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }


        }
        //1.2 filter 库存
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",param.getHasStock()==1));
        //1.2 filter 价格区间 _50 50_100 100_
        if (!StringUtils.isEmpty(param.getSkuPrice())){
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length==2){
                //区间
                skuPrice.gte(s[0]).lte(s[1]);
            }else if (s.length==1){
                if (param.getSkuPrice().startsWith("_")){
                    skuPrice.lte(s[0]);
                }else if (param.getSkuPrice().endsWith("_")){
                    skuPrice.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(skuPrice);
        }
        //把条件都拿来进行分装
        searchSourceBuilder.query(boolQueryBuilder);

        /**
         * 排序，分页，高亮
         */
        //2.1,排序
        if (!StringUtils.isEmpty(param.getSort())){
            String sort = param.getSort();
            //sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder sortOrder=s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            searchSourceBuilder.sort(s[0],sortOrder);
        }
        //2.2 分页
        searchSourceBuilder.from((param.getPageNum()-1)*EsContant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsContant.PRODUCT_PAGESIZE);

        //2.3高亮
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        /**
         *
         * 聚合分析
         */
        //品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);
        //分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);
        //属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);

        searchSourceBuilder.aggregation(attr_agg);

        System.out.println(searchSourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsContant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }
}
