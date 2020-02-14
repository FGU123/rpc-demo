package com.ex.demo.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.springframework.util.StopWatch;

import com.ex.demo.client.global.Environment;
import com.ex.demo.remoting.RpcRequest;
import com.ex.demo.remoting.RpcResponse;

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

	private ExecutorService execService = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
	
	private CompletionService<RpcResponse> completionService = new ExecutorCompletionService<RpcResponse>( execService );
	
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
	public Object invoke(Object proxy, Method method, Object[] args) {
		RpcRequest request = new RpcRequest(); // create and initialize RPC request
		request.setRequestId(UUID.randomUUID().toString());
		request.setClassName(method.getDeclaringClass().getName());
		request.setMethodName(method.getName());
		request.setParameterTypes(method.getParameterTypes());
		request.setArgs(args);
		request.setReturnType(method.getReturnType());
		
		FixedChannelPool pool = Environment.getRegisteredChannelPoolMap().get(Environment.getHost());
		Future<Channel> future = pool.acquire();
		Callable<RpcResponse> callableTask = new Callable<RpcResponse>() {
			
			@Override
			public RpcResponse call() throws Exception {
				future.addListener(new FutureListener<Channel>() {
					
					@Override
					public void operationComplete(Future<Channel> future) throws Exception {
						Channel channel = future.getNow();
						channel.writeAndFlush(request);

						log.info("request [id={}] send data to server channel [id={}]", request.getRequestId(), channel.id());
					}
				});

				// get response data from server, keep blocked till server responses
				RpcResponse response = Environment.getResponseBlockingQueue(request.getRequestId()).take();
				log.info("request [id={}] get response data [{}] from server", request.getRequestId(), response);
				return response;
			}
		};
		
		StopWatch stopWatch = new StopWatch();
		try {
			stopWatch.start();
			
			Object result = completionService.submit(callableTask)
					.get(Environment.getRequestTimeout(), Environment.getRequestTimeunit()).getResult();
			
			stopWatch.stop();
			
			log.info("process rpc request [id={}] consumes {}ms", request.getRequestId(), stopWatch.getTotalTimeMillis());
			
			return result;
			
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			stopWatch.stop();
			log.error("process rpc request [id={}] consumes {}ms, error found ", request.getRequestId(), stopWatch.getTotalTimeMillis(), e);
			pool.release(future.getNow());
			throw new RuntimeException( e ); // hand over the exception to upper layers
		}
	}
}
