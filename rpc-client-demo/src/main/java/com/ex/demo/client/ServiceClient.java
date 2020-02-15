package com.ex.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.ex.demo.client.global.Environment;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceClient implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${rpc.server.host:127.0.0.1}")
	private String host;

	@Value("${rpc.server.port:8888}")
	private int port;
	
	@Value("${rpc.client.channel.pool.size:50}")
	private int channelPoolSize;
	
	@Value("${rpc.client.request.timeout:5000}")
	private int requestTimeout;

	public Bootstrap startClient() {
		Bootstrap bootstrap = new Bootstrap();
		NioEventLoopGroup worker = new NioEventLoopGroup();
		try {
			bootstrap.group(worker);
			bootstrap.channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);
			bootstrap.remoteAddress(host, port);
			Environment.registerChannelPoolMap(RpcChannelPool.getChannelPoolMap(bootstrap, channelPoolSize));
			
			log.info("connected to rpc server /{}:{}", host, port);
			Environment.setHost(host);
			Environment.setRequestTimeout(requestTimeout);
		} catch (Exception e) {
			log.error("connect to rpc server /{}:{}, error ", host, port, e);
		}
		return bootstrap;
	}

	/**
	 * start rpc client after spring application context has been initialized
	 * or refreshed
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		startClient();
	}

}
