package com.ex.demo.client;

import com.ex.demo.client.global.Environment;
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
import io.netty.channel.pool.FixedChannelPool;
import lombok.extern.slf4j.Slf4j;

/** 
 * TODO a better choice could be used instead of fixedChannelPool because of these points: </br>
 *  1. Unable to dynamically control the number of connections (Problem unsolved)</br>
 *  2. Unable to evict any specified channel (Problem solved, {@link com.ex.demo.client.global.Environment#activeChannelMap}) </br>
 *  3. There's no health check mechanism (Problem non-exists, Netty self-implements) </br>
 * </br>
 * A better choice could be like this: </br>
 *  our own implement of ChannelPool to solve problem(s) above </br> 
 */
@Slf4j
public class RpcChannelPool extends AbstractChannelPoolMap<Object, FixedChannelPool> {

	private Bootstrap bootstrap;

	private int poolSize;
	
	private static final int DEFAULT_POOL_SIZE = 50;

	private RpcChannelPool(Bootstrap bootstrap, int poolSize) {
		this.bootstrap = bootstrap;
		this.poolSize = poolSize;
	}
	
	private static volatile RpcChannelPool pool;
	
	public static RpcChannelPool getChannelPoolMap(Bootstrap bootstrap, int poolSize) {
        if (pool == null) {   
            synchronized (RpcChannelPool.class) {
                if (pool == null) {   
                	pool = new RpcChannelPool(bootstrap, poolSize);
                }
            }
        }
        return pool;
    }

	/**
	 * providing another method for default pool size 
	 */
	public static RpcChannelPool getChannelPoolMap(Bootstrap bootstrap) {
		return getChannelPoolMap(bootstrap, DEFAULT_POOL_SIZE);
	}
	
	@Override
	protected FixedChannelPool newPool(Object key) {
		
		ChannelPoolHandler handler = new ChannelPoolHandler() {

			@Override
			public void channelReleased(Channel ch) throws Exception { // callback on released
				log.info("channel [id={}] released back to the pool", ch.id());
				Environment.removeActiveChannel(ch.id());
			}

			@Override
			public void channelAcquired(Channel ch) throws Exception { // callback on acquired
		        log.info("channel [id={}] acquired", ch.id());
		        Environment.addActiveChannel(ch);
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
