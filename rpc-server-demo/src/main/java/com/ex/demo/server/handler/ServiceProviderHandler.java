package com.ex.demo.server.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import com.ex.demo.remoting.RpcRequest;
import com.ex.demo.remoting.RpcResponse;

import cn.hutool.core.map.MapUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * A service-provider that handles receiving msg from client and 
 * then invoke real service method 
 */
@Slf4j
public class ServiceProviderHandler extends ChannelInboundHandlerAdapter {

	private static final Map<String, Object> SERVICE_BEANS = new HashMap<String, Object>();

	private ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private CompletionService<Object> completionService = new ExecutorCompletionService<Object>(es);
	
	/**
	 * if need to boot service provider without spring context, 
	 * need manual hard-coding for the services initialization
	 */
	static {
//		SERVICE_BEANS.put(PersonService.class.getName(), new PersonServiceImpl());
//		SERVICE_BEANS.put(OrderService.class.getName(), new OrderServiceImpl());
	}

	public ServiceProviderHandler() {
		
	}

	public ServiceProviderHandler(Map<String, Object> services) {
		SERVICE_BEANS.putAll(MapUtil.emptyIfNull(services));
	}

	/**
     * receive response and read message from client 
     * and then handle real service method invoking 
     * besides write back handle result if needed.
     */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.info("{}, receive msg from client: {}", this, msg);

		RpcRequest request = (RpcRequest) msg;
		log.info("{} handle {} start", Thread.currentThread(), request.getRequestId());
		long start = System.currentTimeMillis();
//		Object result = handle(request);
		Object result = completionService.submit(()->handle(request)).get();
		log.info("{} handle {} end, consumes {}ms", Thread.currentThread(), request.getRequestId(), (System.currentTimeMillis()-start));

		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());
		response.setResult(result);
		response.setResponseId(UUID.randomUUID().toString());

		ctx.writeAndFlush(response);
		log.info("send msg back to client: {}", response);
	}

	public Object handle(RpcRequest request) {
		String className = request.getClassName();
		Object object = SERVICE_BEANS.get(className);
		Class<?>[] parameterTypes = request.getParameterTypes();
		String methodName = request.getMethodName();
		Object[] args = request.getArgs();

		Class<?> targetClass = object.getClass();

		FastClass fastClass = FastClass.create(targetClass);
		FastMethod method = fastClass.getMethod(methodName, parameterTypes);
		Object invoke = null;
		try {
			invoke = method.invoke(object, args);
		} catch (InvocationTargetException e) {
			log.error("invoke servie method error ", e);
		}
		return invoke;
	}
	
}
