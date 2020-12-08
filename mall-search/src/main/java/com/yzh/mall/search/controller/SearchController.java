package com.yzh.mall.search.controller;

import com.yzh.mall.search.service.MallSearchService;
import com.yzh.mall.search.vo.SearchParam;
import com.yzh.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){

        //获取查询条件
        param.setQueryString(request.getQueryString());
        SearchResult result =mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}
