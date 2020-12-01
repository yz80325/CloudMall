package com.yzh.mall.search.service;

import com.yzh.mall.search.vo.SearchParam;
import com.yzh.mall.search.vo.SearchResult;

public interface MallSearchService {
    /**
     * 检索的所有参数
     * @param param
     */
    SearchResult search(SearchParam param);
}
