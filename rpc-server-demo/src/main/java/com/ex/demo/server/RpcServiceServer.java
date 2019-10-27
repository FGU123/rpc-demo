package com.ex.demo.server;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.ex.demo.codec.RPCDecoder;
import com.ex.demo.codec.RPCEncoder;
import com.ex.demo.remoting.RpcRequest;
import com.ex.demo.remoting.RpcResponse;
import com.ex.demo.server.global.Environment;
import com.ex.demo.server.handler.ServiceProviderHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServiceServer {

	@Value("${rpc.host:127.0.0.1}")
	private String host;

	@Value("${rpc.port:8888}")
	private int port;

	public RpcServiceServer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	NioEventLoopGroup boss = new NioEventLoopGroup();
	NioEventLoopGroup worker = new NioEventLoopGroup();
	ServerBootstrap bootstrap = new ServerBootstrap();
	Channel channel;

	public void run(Map<String, Object> serviceBeans) {
		try {
			bootstrap.group(boss, worker);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast(new RPCDecoder(RpcRequest.class)); // request decode
					p.addLast(new RPCEncoder(RpcResponse.class)); // response encode
					p.addLast(new ServiceProviderHandler(serviceBeans));
				}
			});

			ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
			channel = channelFuture.channel();
			log.info("rpc server running on /{}:{}, config: {}", host, port, bootstrap.config());
			channel.closeFuture().sync(); // 主线程wait
		} catch (Exception e) {
			log.error("rpc server running on /{}:{}, ", host, port, e);
		} finally {
			worker.shutdownGracefully();
			boss.shutdownGracefully();
		}
	}

	public void run() {
		this.run(Environment.getServiceBeans());
	}
}
