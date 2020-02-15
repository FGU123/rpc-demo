package com.ex.demo.remoting;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcResponse implements Serializable {

	private static final long serialVersionUID = 2493857761156142884L;
	
	private String requestId;

	private Object result;

	private String responseId;
	
	private Class<?> returnType;
}
