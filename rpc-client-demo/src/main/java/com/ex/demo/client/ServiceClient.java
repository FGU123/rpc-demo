package com.ex.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.ex.demo.client.global.Environment;
import com.ex.demo.codec.RPCDecoder;
import com.ex.demo.codec.RPCEncoder;
import com.ex.demo.remoting.RpcRequest;
import com.ex.demo.remoting.RpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServiceClient implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${rpc.server.host:127.0.0.1}")
	private String host;

	@Value("${rpc.server.port:8888}")
	private int port;

	public Bootstrap startClient() {
		Bootstrap bootstrap = new Bootstrap();
		NioEventLoopGroup worker = new NioEventLoopGroup();
		try {
			bootstrap.group(worker);
			bootstrap.channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast(new RPCEncoder(RpcRequest.class)); // request encode
					p.addLast(new RPCDecoder(RpcResponse.class)); // response decode
					// considering multiple request concurrency, single channelPipeline may be stuck
					// TODO probably needs multiple channelPipelines
					p.addLast(Environment.getServiceConsumerHandler()); 
				}
			});
			
			bootstrap.connect(host, port).sync();
			log.info("connected to rpc server /{}:{}", host, port);
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
