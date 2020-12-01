package com.yzh.mall.search.controller;

import com.yzh.mall.search.service.MallSearchService;
import com.yzh.mall.search.vo.SearchParam;
import com.yzh.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param,Model model){

        SearchResult result =mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}
