package com.ex.demo.client;

import com.ex.demo.client.handler.RpcRequestHandler;
import com.ex.demo.codec.RPCDecoder;
import com.ex.demo.codec.RPCEncoder;
import com.ex.demo.remoting.RpcRequest;
import com.ex.demo.remoting.RpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcChannelPool<K> extends AbstractChannelPoolMap<K, FixedChannelPool> {

	private static Bootstrap bootstrap;

	private int poolSize = 50;

	private RpcChannelPool() {
		
	}
	
	private static class ChannelPoolMapHolder {
		
		/** 
		 * TODO a better choice could be used instead of fixedChannelPool because of these points:
		 *  1. Unable to dynamically control the number of connections
		 *  2. Unable to evict any specified channel
		 *  3. There's no health check mechanism
		 * 
		 * This better choice could be like this:
		 *  a self implements of ChannelPool to solve those problems above  
		 */
		private static ChannelPoolMap<String, FixedChannelPool> channelPoolMap = new RpcChannelPool<String>();
	}
	
	public static void initChannelPoolMap(Bootstrap bootstrap) {
		RpcChannelPool.bootstrap = bootstrap;
	}
	
	public static ChannelPoolMap<String, FixedChannelPool> getChannelPoolMap() {
		return ChannelPoolMapHolder.channelPoolMap;
	}

	@Override
	protected FixedChannelPool newPool(Object key) {
		ChannelPoolHandler handler = new ChannelPoolHandler() {

			@Override
			public void channelReleased(Channel ch) throws Exception {
				log.info("channel [id={}] released", ch.id());
			}

			@Override
			public void channelAcquired(Channel ch) throws Exception {
				log.info("channel [id={}] acquired", ch.id());
			}

			@Override
			public void channelCreated(Channel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast(new RPCEncoder(RpcRequest.class)); // request encode
				p.addLast(new RPCDecoder(RpcResponse.class)); // response decode
				p.addLast(new RpcRequestHandler());
			}

		};

		return new FixedChannelPool(bootstrap, handler, poolSize);
	}

}
