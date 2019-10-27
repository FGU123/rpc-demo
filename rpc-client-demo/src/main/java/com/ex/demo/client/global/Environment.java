package com.ex.demo.client.global;

import com.ex.demo.client.handler.ServiceConsumerHandler;

public class Environment {

	private static class ServiceConsumerHandlerHolder {
		private static ServiceConsumerHandler serviceConsumerHandler = new ServiceConsumerHandler();
	}
	
	public static ServiceConsumerHandler getServiceConsumerHandler() {
		return ServiceConsumerHandlerHolder.serviceConsumerHandler;
	}
}
