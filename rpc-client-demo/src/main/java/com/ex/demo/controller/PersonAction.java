package com.ex.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ex.demo.annotation.RpcReference;
import com.ex.demo.domain.User;
import com.ex.demo.service.stub.PersonService;

@RestController
@RequestMapping("/person")
public class PersonAction {

	@RpcReference
	public PersonService personService;
	
	@GetMapping( "/hello/say" )
	public Object sayHello(@RequestParam( "name" ) String name) {
		String hello = personService.sayHelloTo(name);
		return ResponseEntity.ok(hello);
	}
	
	@GetMapping( "/{name}" )
	public Object getPerson(@PathVariable( "name" ) String name) {
		User person = personService.getPerson(name);
		return ResponseEntity.ok(person);
	}
}
