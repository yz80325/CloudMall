package com.yzh.mall.product;




import com.yzh.mall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductApplicationTests {

    @Autowired
    CategoryService categoryService;

    @Test
    public void test(){
        Long[] categoryId = categoryService.getCategoryId(255L);
        log.info("完整路径{}", Arrays.asList(categoryId));
    }
}
