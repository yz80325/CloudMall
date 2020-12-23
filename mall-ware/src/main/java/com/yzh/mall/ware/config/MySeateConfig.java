package com.yzh.mall.ware.config;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
public class MySeateConfig {

    @Autowired
    DataSourceProperties dataSourceProperties;

/*    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties){
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }
        //用seata作为代理对象进行数据包装
        return new DataSourceProxy(dataSource);
    }*/
}
