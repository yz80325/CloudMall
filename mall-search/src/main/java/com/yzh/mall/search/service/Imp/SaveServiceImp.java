package com.yzh.mall.search.service.Imp;

import com.alibaba.fastjson.JSON;
import com.yzh.common.to.es.SkuEsModel;
import com.yzh.mall.search.config.MallElasticSearchConfig;
import com.yzh.mall.search.constant.EsContant;
import com.yzh.mall.search.service.SaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaveServiceImp implements SaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean SaveSkuModel(List<SkuEsModel> skuEsModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsContant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, MallElasticSearchConfig.COMMON_OPTIONS);
        boolean b = bulk.hasFailures();
        if (b){
            List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
                return item.getId();
            }).collect(Collectors.toList());
            log.error("商品上架错误{}",collect);
        }else {
            log.info("商品上架成功");
        }
        return b;

    }
}
