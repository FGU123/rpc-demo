package com.ex.demo.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ex.demo.client.global.Environment;
import com.ex.demo.client.handler.ServiceConsumerHandler;
import com.ex.demo.remoting.RpcRequest;

/**
 * provides a proxy for rpc referenced interface
 * Indeed, create a rpc request, then async call ServiceConsumerHandler 
 * besides this handler would be put into a ThreadPool 
 */
public class SeviceConsumerProxy implements InvocationHandler {

	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static class ServiceConsumerProxyHolder {
		private static SeviceConsumerProxy handler = new SeviceConsumerProxy();
	}
	
	public static SeviceConsumerProxy getInstance() {
		return ServiceConsumerProxyHolder.handler;
	}
	
	public Object createProxy(Class<?> serviceClass) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { serviceClass }, this);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求
		request.setRequestId(UUID.randomUUID().toString());
		request.setClassName(method.getDeclaringClass().getName());
		request.setMethodName(method.getName());
		request.setParameterTypes(method.getParameterTypes());
		request.setArgs(args);
		ServiceConsumerHandler handler = Environment.getServiceConsumerHandler();
		handler.setParams(request);
		return executor.submit(handler).get(); // TODO considering multiple handler
	}
}
