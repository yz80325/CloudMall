package com.yzh.mall.product.web;

import com.yzh.mall.product.entity.CategoryEntity;
import com.yzh.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {


    @Autowired
    CategoryService categoryService;
    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        List<CategoryEntity> category1=categoryService.getCategory1();

        model.addAttribute("categorys",category1);
        return "index";
    }
}
