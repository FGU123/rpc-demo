package com.ex.demo.client.global;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

public class Environment {

	private static ConcurrentHashMap<String, SynchronousQueue<Object>> resultBlockingMap = new ConcurrentHashMap<>();
	
	public static SynchronousQueue<Object> getResultBlockingQueue(String key) {
		resultBlockingMap.putIfAbsent(key, new SynchronousQueue<Object>());
		return resultBlockingMap.get(key);
	}
	
	public static ConcurrentHashMap<String, SynchronousQueue<Object>> getResultBlockingMap() {
		return resultBlockingMap;
	}

	public static void setRequestBlockingMap(ConcurrentHashMap<String, SynchronousQueue<Object>> requestBlockingMap) {
		Environment.resultBlockingMap = requestBlockingMap;
	}
	
	private static String host;

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		Environment.host = host;
	}
}
