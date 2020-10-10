package com.yzh.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
* 1,整合MyBatis-Plus
*  <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.0</version>
        </dependency>
  2，配置
  * 1，配置数据源
  *   1),导入数据库驱动。
  *   2),在yml配置数据源
  * 2,配置Mybatis-Plus：
  *   1）,使用MapperScan
  *   2),告诉系统文件在哪里
*
* */
@MapperScan("com.yzh.mall.order.dao")
@SpringBootApplication
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

}
