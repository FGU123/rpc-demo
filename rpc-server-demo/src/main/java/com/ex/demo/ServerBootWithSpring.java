package com.ex.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ex.demo.server.scan.RpcComponentScan;

@SpringBootApplication
@RpcComponentScan
public class ServerBootWithSpring {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(ServerBootWithSpring.class, args);
	}
}
