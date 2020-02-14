package com.ex.demo.client.global;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

import com.ex.demo.remoting.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;

/**
 * context of a single rpc client
 */
public class Environment {

	private static ConcurrentHashMap<String, SynchronousQueue<RpcResponse>> responseBlockingMap = new ConcurrentHashMap<>();
	
	public static SynchronousQueue<RpcResponse> getResponseBlockingQueue(String key) {
		responseBlockingMap.putIfAbsent(key, new SynchronousQueue<RpcResponse>());
		return responseBlockingMap.get(key);
	}
	
	public static ConcurrentHashMap<String, SynchronousQueue<RpcResponse>> getResponseBlockingMap() {
		return responseBlockingMap;
	}

	/**
	 * providing a method to specify a ResponseBlockingMap 
	 */
	public static void setResponseBlockingMap(ConcurrentHashMap<String, SynchronousQueue<RpcResponse>> responseBlockingMap) {
		Environment.responseBlockingMap = responseBlockingMap;
	}
	
	private static ConcurrentHashMap<ChannelId, Channel> activeChannelMap = new ConcurrentHashMap<>();
	
	public static Channel getActiveChannel(ChannelId channelId) {
		return activeChannelMap.get(channelId);
	}
	
	public static ConcurrentHashMap<ChannelId, Channel> getActiveChannelMap() {
		return activeChannelMap;
	}
	
	public static void addActiveChannel(Channel channel) {
		activeChannelMap.put(channel.id(), channel);
	}
	
	public static void removeActiveChannel(ChannelId channelId) {
		activeChannelMap.remove(channelId);
	}
	
	private static String host;

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		Environment.host = host;
	}

	private static AbstractChannelPoolMap<Object, FixedChannelPool> channelPoolMap;
	
	public static AbstractChannelPoolMap<Object, FixedChannelPool> getRegisteredChannelPoolMap() {
		return channelPoolMap;
	}
	
	public static void registerChannelPoolMap(AbstractChannelPoolMap<Object, FixedChannelPool> channelPool) {
		Environment.channelPoolMap = channelPool;
	}
}
