package com.ex.demo.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import com.ex.demo.client.global.Environment;
import com.ex.demo.remoting.RpcRequest;

import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * provides a proxy for rpc referenced interface Indeed, create a rpc request,
 * then async call ServiceConsumerHandler besides this handler would be put into
 * a ThreadPool
 */
@Slf4j
public class SeviceConsumerProxy implements InvocationHandler {

	private static class ServiceConsumerProxyHolder {
		private static SeviceConsumerProxy handler = new SeviceConsumerProxy();
	}

	public static SeviceConsumerProxy getInstance() {
		return ServiceConsumerProxyHolder.handler;
	}

	public Object createProxy(Class<?> serviceClass) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { serviceClass },
				this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求
		request.setRequestId(UUID.randomUUID().toString());
		request.setClassName(method.getDeclaringClass().getName());
		request.setMethodName(method.getName());
		request.setParameterTypes(method.getParameterTypes());
		request.setArgs(args);
		FixedChannelPool pool = Environment.getRegisteredChannelPoolMap().get(Environment.getHost());
		Future<Channel> future = pool.acquire();
		future.addListener(new FutureListener<Channel>() {
			@Override
			public void operationComplete(Future<Channel> future) throws Exception {
				Channel channel = future.getNow();
				channel.writeAndFlush(request);

				log.debug("channel id: " + channel.id());
			}
		});

		// 获取服务端返回的数据
		return Environment.getResultBlockingQueue(request.getRequestId()).take();
	}
}
