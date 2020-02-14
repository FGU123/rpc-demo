package com.ex.demo.server.global;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Environment {

	private static Map<String, Object> serviceBeans = new HashMap<>();

	public static Map<String, Object> getServiceBeans() {
		return serviceBeans;
	}

	public static void setServiceBeans(Map<String, Object> serviceBeans) {
		Environment.serviceBeans = serviceBeans;
	}

	private static int serviceProcessTimeout;

	public static int getServiceProcessTimeout() {
		return serviceProcessTimeout <= 0 ? Integer.MAX_VALUE : serviceProcessTimeout;
	}

	public static void setServiceProcessTimeout(int serviceProcessTimeout) {
		Environment.serviceProcessTimeout = serviceProcessTimeout;
	}

	public static TimeUnit getServiceProcessTimeunit() {
		return TimeUnit.MILLISECONDS; // TODO support property specify
	}
}
