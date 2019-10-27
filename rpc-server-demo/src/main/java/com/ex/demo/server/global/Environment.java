package com.ex.demo.server.global;

import java.util.HashMap;
import java.util.Map;

public class Environment {

	private static Map<String, Object> serviceBeans = new HashMap<>();

	public static Map<String, Object> getServiceBeans() {
		return serviceBeans;
	}

	public static void setServiceBeans(Map<String, Object> serviceBeans) {
		Environment.serviceBeans = serviceBeans;
	}
}
