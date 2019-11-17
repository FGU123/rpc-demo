package com.ex.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.ex.demo.client.global.Environment;
import com.ex.demo.client.handler.RPCRequestHandler;
import com.ex.demo.codec.RPCDecoder;
import com.ex.demo.codec.RPCEncoder;
import com.ex.demo.remoting.RpcRequest;
import com.ex.demo.remoting.RpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceClient implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${rpc.server.host:127.0.0.1}")
	private String host;

	@Value("${rpc.server.port:8888}")
	private int port;

	public static ChannelPoolMap<String, FixedChannelPool> poolMap;
	
	public Bootstrap startClient() {
		Bootstrap bootstrap = new Bootstrap();
		NioEventLoopGroup worker = new NioEventLoopGroup();
		try {
			bootstrap.group(worker);
			bootstrap.channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);
			bootstrap.remoteAddress(host, port);
			poolMap = new AbstractChannelPoolMap<String, FixedChannelPool>() {

				@Override
				protected FixedChannelPool newPool(String key) {
					ChannelPoolHandler handler = new ChannelPoolHandler() {

						@Override
						public void channelReleased(Channel ch) throws Exception {
							log.info("channelAcquired......");
						}

						@Override
						public void channelAcquired(Channel ch) throws Exception {
							log.info("channelAcquired......");
						}

						@Override
						public void channelCreated(Channel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new RPCEncoder(RpcRequest.class)); // request encode
							p.addLast(new RPCDecoder(RpcResponse.class)); // response decode
							p.addLast(new RPCRequestHandler());
						}
						
					};
					return new FixedChannelPool(bootstrap, handler, 50);
				}
				
			};
			
			log.info("connected to rpc server /{}:{}", host, port);
			Environment.host = host;
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
