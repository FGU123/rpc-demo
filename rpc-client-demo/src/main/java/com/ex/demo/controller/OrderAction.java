package com.ex.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ex.demo.annotation.RpcReference;
import com.ex.demo.domain.Order;
import com.ex.demo.service.stub.OrderService;

@RestController
public class OrderAction {

	@RpcReference
	public OrderService orderService;
	
	@GetMapping( "/order" )
	public Object sayHello(@RequestParam( "id" ) Long id) {
		Order order = orderService.getOrder(id);
		return ResponseEntity.ok(order);
	}
	
	@GetMapping( "/orders" )
	public Object getPerson() {
		List<Order> orders = orderService.getOrders();
		return ResponseEntity.ok(orders);
	}
	
	@PutMapping( "/order" )
	public Object updatePerson(@RequestParam( "id" ) Long id) {
		orderService.updateOrder(id);
		return ResponseEntity.ok("updated");
	}
}
