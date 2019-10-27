package com.ex.demo.server.boot;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistredRpcComponents {

	private Map<String, Object> serviceBeans;
}
