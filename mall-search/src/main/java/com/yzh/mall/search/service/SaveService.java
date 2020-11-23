package com.yzh.mall.search.service;

import com.yzh.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


public interface SaveService {


    boolean SaveSkuModel(List<SkuEsModel> skuEsModels) throws IOException;
}
