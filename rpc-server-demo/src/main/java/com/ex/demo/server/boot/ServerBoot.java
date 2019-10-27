package com.ex.demo.server.boot;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.ex.demo.server.RpcServiceServer;
import com.ex.demo.server.global.Environment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Configuration
public class ServerBoot implements ApplicationListener<ContextRefreshedEvent>  {

	@Value("${rpc.host:127.0.0.1}")
	private String host;

	@Value("${rpc.port:8888}")
	private int port;
	
	private volatile boolean active = false;
	
	private RpcServiceServer server;
	
	public void startServer() {
		this.startServer(null);
	}
	
	/**
	 *  TODO considering hot deployment, need to avoid the {@link BindException}: port already in use  
	 */
	public void startServer( Map<String, Object> map ) {
		if( active ) {
			log.warn("already exists active server!");
			return;
		}
		server = new RpcServiceServer(host, port);
		if( null == map ) {
			new Thread(() -> server.run()).start();
		} else {
			new Thread(() -> server.run(map)).start();
		}
		active = true;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		startServer(Environment.getServiceBeans());
	}
}
