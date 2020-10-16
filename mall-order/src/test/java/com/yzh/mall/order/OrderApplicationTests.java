package com.yzh.mall.order;

import com.yzh.mall.order.controller.OrderController;
import com.yzh.mall.order.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class OrderApplicationTests {

	@Autowired
	OrderController orderController;

	@Test
	void contextLoads() {
		OrderEntity orderEntity=new OrderEntity();
		orderEntity.setMemberId(100L);
		orderController.save(orderEntity);
		System.out.println("保存成功");
	}

}
