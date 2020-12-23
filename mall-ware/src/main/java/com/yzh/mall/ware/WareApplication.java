package com.yzh.mall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableFeignClients(basePackages = "com.yzh.mall.ware.feign")
@EnableDiscoveryClient
@EnableRabbit
@EnableTransactionManagement
@SpringBootApplication
public class WareApplication {

	public static void main(String[] args) {
		SpringApplication.run(WareApplication.class, args);
	}

}
