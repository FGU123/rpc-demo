package com.ex.demo.service.impl;

import java.time.LocalTime;

import com.ex.demo.annotation.RpcService;
import com.ex.demo.domain.User;
import com.ex.demo.service.stub.PersonService;

import cn.hutool.core.util.RandomUtil;

@RpcService
public class PersonServiceImpl implements PersonService {

	@Override
	public String sayHelloTo(String name) {
		User user = mockData(name);
		return "[ " + LocalTime.now() + "] Hello, this person is " + user.getName() + ", age is " + user.getAge();
	}

	@Override
	public User getPerson(String name) {
		return mockData(name);
	}

	private User mockData(String name) {
		return User.builder().name(name).age(RandomUtil.randomInt(100)).build();
	}
}
