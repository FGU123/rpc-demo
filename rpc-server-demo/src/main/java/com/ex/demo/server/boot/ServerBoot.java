package com.ex.demo.server.boot;

import com.ex.demo.server.RpcServiceServer;
import com.ex.demo.server.global.Environment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Configuration
public class ServerBoot implements ApplicationListener<ApplicationContextEvent>  {

	@Value("${rpc.host:127.0.0.1}")
	private String host;

	@Value("${rpc.port:8888}")
	private int port;

	@Value("${rpc.server.service.process.timeout:5000}")
	private int serviceProcessTimeout;

	private volatile boolean active = false;
	
	private RpcServiceServer server;
	
	public void startServer() {
		this.startServer(null);
	}

	public void startServer( Map<String, Object> map ) {
		if( active ) {
			log.warn("already exists any other active server!");
			return;
		}
		server = new RpcServiceServer(host, port);
		if( null == map ) {
			new Thread(() -> server.run()).start();
		} else {
			new Thread(() -> server.run(map)).start();
		}
		active = true;
		Environment.setServiceProcessTimeout(serviceProcessTimeout);
	}

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if( event instanceof ContextStoppedEvent || event instanceof ContextClosedEvent) {
			server.shutdown();
		} else if( event instanceof ContextRefreshedEvent || event instanceof ContextStartedEvent) {
			startServer( Environment.getServiceBeans() );
		}
		log.info("processing application event {}", event);
	}
}
