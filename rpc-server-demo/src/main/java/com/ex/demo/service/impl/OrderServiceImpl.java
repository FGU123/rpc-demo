package com.ex.demo.service.impl;

import java.util.List;

import com.ex.demo.annotation.RpcService;
import com.ex.demo.domain.Order;
import com.ex.demo.service.stub.OrderService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

@RpcService
@Slf4j
public class OrderServiceImpl implements OrderService {

	@Override
	public List<Order> getOrders() {
//		ThreadUtil.safeSleep(10000);
		return mockDatas();
	}

	@Override
	public Order getOrder(Long id) {
		return mockData(id);
	}

	private List<Order> mockDatas() {
		return CollUtil.newArrayList(mockData(RandomUtil.randomLong(8000000)), mockData(RandomUtil.randomLong(8000000)));
	}

	private Order mockData(Long id) {
		return Order.builder().id(id).buyerId(RandomUtil.randomLong(8000000)).amount(RandomUtil.randomDouble(8000000)).build();
	}

	@Override
	public void updateOrder(Long id) {
		mockUpdateData(id);
	}

	private void mockUpdateData(Long id) {
		// do something to mock update
		log.info("mock update data, id={}", id);
	}

}
