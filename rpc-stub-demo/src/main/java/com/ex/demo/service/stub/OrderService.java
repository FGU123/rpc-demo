package com.ex.demo.service.stub;

import java.util.List;

import com.ex.demo.domain.Order;

public interface OrderService {

	List<Order> getOrders();

	Order getOrder(Long id);
	
	void updateOrder(Long id);
}
