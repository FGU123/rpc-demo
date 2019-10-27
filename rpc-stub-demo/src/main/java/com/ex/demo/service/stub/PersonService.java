package com.ex.demo.service.stub;

import com.ex.demo.domain.User;

public interface PersonService {

	String sayHelloTo(String name);
	
	User getPerson(String name);
}
